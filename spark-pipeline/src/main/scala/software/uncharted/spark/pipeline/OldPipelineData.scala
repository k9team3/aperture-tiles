package software.uncharted.spark.pipeline



import scala.language.higherKinds
import scala.language.implicitConversions
import scala.language.existentials

import software.uncharted.spark.pipeline.PipelineData._
import _root_.software.uncharted.spark.pipeline.typesupport.Foldable
import _root_.software.uncharted.spark.pipeline.typesupport.TypeOrdinal
import _root_.software.uncharted.spark.pipeline.typesupport.TypeOrdinal._



/**
 * Data to be passed through a pipeline.  This can represent one or more pieces of input data.
 *
 * Created by nkronenfeld on 10/14/2015.
 */
sealed trait PipelineData {
  type Head
  type Tail <: PipelineData
  type Length = FoldR[TypeOrdinal, Foldable.Inc, _0]


  // Allow type folding of pipeline data
  type FoldR[Value, F <: Foldable[Any, Value], I <: Value] <: Value
  def foldR[Value, F <: Foldable[Any, Value], I <: Value](f: F, i: I): FoldR[Value, F, I]

  type FoldL[Value, F <: Foldable[Any, Value], I <: Value] <: Value
  def foldL[Value, F <: Foldable[Any, Value], I <: Value](f: F, i: I): FoldL[Value, F, I]

  type toI[N <: TypeOrdinal] <: PipelineDataIndex

  type Fun[T]
  def apply[T](f: Fun[T]): T
}
object PipelineData extends PipelineDataApplyOps
{
  // type alias for writing PDCons[H, T] as H :: T
  type ::[H, T <: PipelineData] = PDCons[H, T]
  // extractor for writing
  //  case PDCons(head, tail) =>
  //as
  //  case head :: tail =>
  object :: {
    def unapply[H,T<:PipelineData](list: PDCons[H,T]) = Some((list.head,list.tail))
  }

  import PipelineDataIndex._

  type :::[A <: PipelineData, B <: PipelineData] = A#FoldR[PipelineData, AppPDCons.type, B]

  //  object Concat extends Foldable[PipelineData, PipelineData] {
  //    type Apply[N <: PipelineData, H <: PipelineData] = N ::: H
  //    def apply[A <: PipelineData, B <: PipelineData](a: A, b: B) = a ::: b
  //  }

  implicit def pdlistOps[B <: PipelineData](b: B): PDListOps[B] =
    new PDListOps[B] {
      def :::[A <: PipelineData](a: A): A#FoldR[PipelineData, AppPDCons.type, B] = a.foldR[PipelineData, AppPDCons.type, B](AppPDCons, b)
      def ::[A](a: A) = PDCons(a, b)
    }

  object AppPDCons extends Foldable[Any, PipelineData] {
    type Apply[N <: Any, H <: PipelineData] = N :: H
    def apply[A,B <: PipelineData](a: A, b: B) = PDCons(a, b)
  }

  //  implicit def pdconsOps[H, T <: PipelineData](pd: H :: T): PDConsOps[H, T] =
  //    new PDConsOps[H, T] {
  //      def pipelinedata = pd
  //      def i[N <: TypeOrdinal](implicit i: H::T => Ind[N]) = i(pd)
  //    }
}



/*
 * Standard chain pipeline datum, with link to next
 */
final case class PDCons[H, T <: PipelineData](head : H, tail : T) extends PipelineData
{
  type Head = H
  type Tail = T
  def ::[HH](newHead : HH) = PDCons(newHead, this)

  type FoldR[Value, F <: Foldable[Any, Value], I <: Value] = F#Apply[H, tail.FoldR[Value, F, I]]
  def foldR[Value, F <: Foldable[Any, Value], I <: Value](f: F, i: I): FoldR[Value, F, I] = f(head, tail.foldR[Value, F, I](f, i) )

  type FoldL[Value, F <: Foldable[Any, Value], I <: Value] = tail.FoldL[Value, F, F#Apply[H, I]]
  def foldL[Value, F <: Foldable[Any, Value], I <: Value](f: F, i: I): FoldL[Value, F, I] = tail.foldL[Value, F, F#Apply[H,I]](f, f(head, i))

  type IN[M <: TypeOrdinal] = PipelineDataIndexN[H, tail.toI[M]]
  type toI[N <: TypeOrdinal] = N#Match[ IN, PipelineDataIndex0[H, T], PipelineDataIndex]

  type Fun[T] = H => tail.Fun[T]
  def apply[T](f: Fun[T]): T = tail( f(head) )

  override def toString = head + " :: " + tail
}


/*
 * Terminal pipeline datum (with no data)
 */
sealed class PDNil extends PipelineData
{
  type Head = Nothing
  type Tail = PDNil
  type Wrap[M[_]] = PDNil
  def ::[T](v : T) = PDCons(v, this)

  type FoldL[Value, F <: Foldable[Any, Value], I <: Value] = I
  def foldL[Value, F <: Foldable[Any, Value], I <: Value](f: F, i: I) = i

  type FoldR[Value, F <: Foldable[Any, Value], I <: Value] = I
  def foldR[Value, F <: Foldable[Any, Value], I <: Value](f: F, i: I) = i

  type toI[N <: TypeOrdinal] = Nothing

  type Fun[T] = T
  def apply[T](t: Fun[T]) = t
}
// One must be careful with use of this - it is a slightly different type than class PDNil; if one can specifically
// assign the type, that's fine, but where the type is inferred, and there is a warning of PDNil.type vs. PDNil, just
// use new PDNil instead..
object PDNil extends PDNil



sealed trait PipelineDataApplyOps
{
  implicit def pdApplyNil(h: PDNil) : PDNil => PDNil =
    _ => PDNil

  implicit def happlyCons[InH, OutH, TF <: PipelineData, TIn <: PipelineData, TOut <: PipelineData]
  (implicit applyTail: TF => TIn => TOut):
  ((InH => OutH) :: TF) => ( (InH :: TIn) => (OutH :: TOut) ) = h =>

    in =>	PDCons( h.head(in.head), applyTail(h.tail)(in.tail) )

  def partial[H <: PipelineData, In <: PipelineData, Out <: PipelineData](h: H)(implicit toApply: H => In => Out): In => Out =
    toApply(h)
  def happly[H <: PipelineData, In <: PipelineData, Out <: PipelineData](h: H)(in: In)(implicit toApply: H => In => Out): Out =
    toApply(h)(in)
}
sealed trait PDConsOps[H, T <: PipelineData] {
  def pipelinedata: H :: T
  def i[N <: TypeOrdinal](implicit it: H::T => Ind[N]): Ind[N]
  type Ind[N <: TypeOrdinal] = PDCons[H, T]#toI[N]
}
sealed trait PDListOps[B <: PipelineData] {
  def :::[A <: PipelineData](a: A): A ::: B
  def ::[A](b: A): A :: B
}
sealed class TipDummy[S, PD <: PipelineData](val pd: PD)
