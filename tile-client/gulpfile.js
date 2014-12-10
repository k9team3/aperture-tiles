var gulp = require('gulp'),
  	del = require('del'),
    concat = require('gulp-concat'),
    csso = require('gulp-csso'),
    jshint = require('gulp-jshint'),
    browserify = require('browserify'),
    source = require('vinyl-source-stream'),
    buffer = require('vinyl-buffer'),
    uglify = require('gulp-uglify');


function bundle( b, minify ) {
    return b.bundle()
        .on( 'error', function( e ) {
            console.log( e );
        })
        .pipe( source( 'tiles.js' ) )
        .pipe( gulp.dest( 'build' ) );
}

function bundleMin( b, minify ) {
    return b.bundle()
        .on( 'error', function( e ) {
            console.log( e );
        })
        .pipe( source( 'tiles.min.js' ) )
        .pipe( buffer() )
        .pipe( uglify() )
        .pipe( gulp.dest( 'build' ) );
}

function build( root ) {
    var b = browserify( root, { 
            debug: true,
            standalone: 'tiles'
        });
    return bundle( b );
}

function buildMin( root ) {
    var b = browserify( root, { 
            standalone: 'tiles'
        });
    return bundleMin( b );
}

function handleError( err ) {
    console.log( err.toString() );
    this.emit('end');
}

gulp.task('clean', function () {
   	del([ 'build/*']);
});

gulp.task('lint', function() {
     return gulp.src( ['src/js/**/*.js', '!src/js/openlayers/*.js'] )
             .pipe( jshint() )
             .pipe( jshint('.jshintrc') )
             .pipe( jshint.reporter('jshint-stylish') );
});

gulp.task('build-min-css', function () {
    return gulp.src( 'src/css/*.css' )
        .pipe( csso() )
        .pipe( concat('tiles.min.css') )
        .pipe( gulp.dest('build') );
});

gulp.task('build-css', function () {
    return gulp.src( 'src/css/*.css' )
        .pipe( concat('tiles.css') )
        .pipe( gulp.dest('build') );
});

gulp.task('build-min-js', [ 'lint' ], function() {
    return buildMin( './src/js/api.js' );
});

gulp.task('build-js', [ 'lint' ], function() {
    return build( './src/js/api.js' );
});

gulp.task('build-rest-min-js', [ 'lint' ], function() {
    return buildMin( './src/js/rest-api.js' );
});

gulp.task('build-rest-js', [ 'lint' ], function() {
    return build( './src/js/rest-api.js' );
});

gulp.task('build', [ 'clean' ], function() {
    gulp.start( 'build-js' );
    gulp.start( 'build-rest-js' );
    gulp.start( 'build-css' );   
    gulp.start( 'build-min-js' );;
    gulp.start( 'build-rest-min-js' );
    gulp.start( 'build-min-css' );
});

gulp.task('default', [ 'build' ], function() {
});