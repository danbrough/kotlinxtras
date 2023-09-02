package org.danbrough.kotlinxtras.wolfssh

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.library.xtrasCreateLibrary
import org.danbrough.kotlinxtras.library.xtrasRegisterSourceTask
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project


object WolfSSH {
  const val extensionName = "wolfSSH"
  const val sourceURL = "https://github.com/wolfSSL/wolfssh.git"
}

class WolfSSHPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("WolfSSHPlugin.apply()")
  }
}

const val WOLFSSH_VERSION = "1.4.14"
const val WOLFSSH_COMMIT = "v1.4.14-stable"

@XtrasDSLMarker
fun Project.xtrasWolfSSH(
  ssl:XtrasLibrary,
  name: String = WolfSSH.extensionName,
  version: String = properties.getOrDefault("wolfssh.version",WOLFSSH_VERSION).toString(),
  commit: String = properties.getOrDefault("wolfssh.commit",  WOLFSSH_COMMIT).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, version) {
  gitSource(WolfSSH.sourceURL, commit)
  configure()

  supportedTargets.forEach { target ->
    val autogenTaskName = prepareSourceTaskName(target)
    xtrasRegisterSourceTask(autogenTaskName, target) {
      commandLine("./autogen.sh")
      outputs.file(workingDir.resolve("configure"))
    }

    val configureTaskName = configureTaskName(target)
    xtrasRegisterSourceTask(configureTaskName, target) {
      dependsOn(ssl.extractArchiveTaskName(target),autogenTaskName)

      outputs.file(workingDir.resolve("Makefile"))
      val configureOptions = mutableListOf(
        "./configure",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target)}",
        "--with-wolfssl=${ssl.libsDir(target)}",
        "--enable-static",
        )

      commandLine(configureOptions)

    }

    val buildTaskName = buildTaskName(target)
    xtrasRegisterSourceTask(buildTaskName, target) {
      dependsOn(configureTaskName)
      outputs.dir(buildDir(target))
      commandLine("make", "install")
    }
  }
}


/**
`configure' configures wolfssh 1.4.14 to adapt to many kinds of systems.

Usage: ./configure [OPTION]... [VAR=VALUE]...

To assign environment variables (e.g., CC, CFLAGS...), specify them as
VAR=VALUE.  See below for descriptions of some of the useful variables.

Defaults for the options are specified in brackets.

Configuration:
  -h, --help              display this help and exit
      --help=short        display options specific to this package
      --help=recursive    display the short help of all the included packages
  -V, --version           display version information and exit
  -q, --quiet, --silent   do not print `checking ...' messages
      --cache-file=FILE   cache test results in FILE [disabled]
  -C, --config-cache      alias for `--cache-file=config.cache'
  -n, --no-create         do not create output files
      --srcdir=DIR        find the sources in DIR [configure dir or `..']

Installation directories:
  --prefix=PREFIX         install architecture-independent files in PREFIX
                          [/usr/local]
  --exec-prefix=EPREFIX   install architecture-dependent files in EPREFIX
                          [PREFIX]

By default, `make install' will install all the files in
`/usr/local/bin', `/usr/local/lib' etc.  You can specify
an installation prefix other than `/usr/local' using `--prefix',
for instance `--prefix=$HOME'.

For better control, use the options below.

Fine tuning of the installation directories:
  --bindir=DIR            user executables [EPREFIX/bin]
  --sbindir=DIR           system admin executables [EPREFIX/sbin]
  --libexecdir=DIR        program executables [EPREFIX/libexec]
  --sysconfdir=DIR        read-only single-machine data [PREFIX/etc]
  --sharedstatedir=DIR    modifiable architecture-independent data [PREFIX/com]
  --localstatedir=DIR     modifiable single-machine data [PREFIX/var]
  --runstatedir=DIR       modifiable per-process data [LOCALSTATEDIR/run]
  --libdir=DIR            object code libraries [EPREFIX/lib]
  --includedir=DIR        C header files [PREFIX/include]
  --oldincludedir=DIR     C header files for non-gcc [/usr/include]
  --datarootdir=DIR       read-only arch.-independent data root [PREFIX/share]
  --datadir=DIR           read-only architecture-independent data [DATAROOTDIR]
  --infodir=DIR           info documentation [DATAROOTDIR/info]
  --localedir=DIR         locale-dependent data [DATAROOTDIR/locale]
  --mandir=DIR            man documentation [DATAROOTDIR/man]
  --docdir=DIR            documentation root [DATAROOTDIR/doc/wolfssh]
  --htmldir=DIR           html documentation [DOCDIR]
  --dvidir=DIR            dvi documentation [DOCDIR]
  --pdfdir=DIR            pdf documentation [DOCDIR]
  --psdir=DIR             ps documentation [DOCDIR]

Program names:
  --program-prefix=PREFIX            prepend PREFIX to installed program names
  --program-suffix=SUFFIX            append SUFFIX to installed program names
  --program-transform-name=PROGRAM   run sed PROGRAM on installed program names

System types:
  --build=BUILD     configure for building on BUILD [guessed]
  --host=HOST       cross-compile to build programs to run on HOST [BUILD]
  --target=TARGET   configure for building compilers for TARGET [HOST]

Optional Features:
  --disable-option-checking  ignore unrecognized --enable/--with options
  --disable-FEATURE       do not include FEATURE (same as --enable-FEATURE=no)
  --enable-FEATURE[=ARG]  include FEATURE [ARG=yes]
  --enable-silent-rules   less verbose build output (undo: "make V=1")
  --disable-silent-rules  verbose build output (undo: "make V=0")
  --enable-static[=PKGS]  build static libraries [default=no]
  --enable-shared[=PKGS]  build shared libraries [default=yes]
  --enable-fast-install[=PKGS]
                          optimize for fast installation [default=yes]
  --enable-dependency-tracking
                          do not reject slow dependency extractors
  --disable-dependency-tracking
                          speeds up one-time build
  --disable-libtool-lock  avoid locking (might break parallel builds)
  --enable-debug          Add debug code/turns off optimizations (yes|no)
                          [default=no]
  --enable-jobserver[=no/yes/#] default=yes
                        Enable up to # make jobs
                        yes: enable one more than CPU count

  --disable-inline        Disable inline functions (default: enabled)
  --disable-examples      Disable examples (default: enabled)
  --enable-keygen         Enable key generation (default: disabled)
  --enable-scp            Enable scp support (default: disabled)
  --enable-sftp           Enable SFTP support (default: disabled)
  --enable-sshd           Enable SSHD support (default: disabled)
  --enable-fwd            Enable TCP/IP Forwarding support (default: disabled)
  --disable-term          Disable pseudo-terminal support (default: enabled)
  --enable-shell          Enable echoserver shell support (default: disabled)
  --enable-agent          Enable ssh-agent support (default: disabled)
  --enable-certs          Enable X.509 cert support (default: disabled)
  --enable-smallstack     Enable small stack (default: disabled)
  --enable-all            Enable all wolfSSH features (default: disabled)
  --enable-distro         Enable wolfSSH distro build (default: disabled)

Optional Packages:
  --with-PACKAGE[=ARG]    use PACKAGE [ARG=yes]
  --without-PACKAGE       do not use PACKAGE (same as --with-PACKAGE=no)
  --with-pic[=PKGS]       try to use only PIC/non-PIC objects [default=use
                          both]
  --with-aix-soname=aix|svr4|both
                          shared library versioning (aka "SONAME") variant to
                          provide on AIX, [default=aix].
  --with-gnu-ld           assume the C compiler uses GNU ld [default=no]
  --with-sysroot[=DIR]    Search for dependent libraries within DIR (or the
                          compiler's sysroot if not specified).
  --with-liboqs=PATH      Path to liboqs install (default /usr/local)
                          EXPERIMENTAL!
  --with-wolfssl=PATH     PATH to wolfssl install (default /usr/local)
  --with-pam=PATH         PATH to directory with the PAM library

Some influential environment variables:
  CC          C compiler command
  CFLAGS      C compiler flags
  LDFLAGS     linker flags, e.g. -L<lib dir> if you have libraries in a
              nonstandard directory <lib dir>
  LIBS        libraries to pass to the linker, e.g. -l<library>
  CPPFLAGS    (Objective) C/C++ preprocessor flags, e.g. -I<include dir> if
              you have headers in a nonstandard directory <include dir>
  LT_SYS_LIBRARY_PATH
              User-defined run-time library search path.
  CPP         C preprocessor

Use these variables to override the choices made by `configure' or to help
it to find libraries and programs with nonstandard names/locations.

Report bugs to <support@wolfssl.com>.
wolfssh home page: <https://www.wolfssl.com>.
**/
