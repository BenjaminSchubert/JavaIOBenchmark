\NeedsTeXFormat{LaTeX2e}[1995/12/01]

\ProvidesClass{lab_report}[2006/03/02 Document class (Basic Lab Report)]

\LoadClass{article}

\RequirePackage{sphinx}
\RequirePackage[a4paper,left=2.5cm,right=2.5cm,top=2.5cm,bottom=2.5cm]{geometry}

\renewcommand{\maketitle}{
   \@author \hfill \@date \vspace{-2ex}
  \rule{\textwidth}{1pt} \vspace{-1ex}

  \begin{center}
    {\rm\Huge\py@HeaderFamily \@title}
  \end{center}
  \setcounter{footnote}{0}
  \let\maketitle\relax
}


\renewcommand{\tableofcontents}{ % we don't need a table of content
    \vspace{2ex}
}

\newcounter {chapter}
\@addtoreset{section}{chapter}

\renewcommand\thechapter{\@arabic\c@chapter}
\renewcommand\thesection{\thechapter.\@arabic\c@section}


\newcommand\chapter{
   \@startsection {chapter}{1}{\z@}%
   {-3.5ex \@plus -1ex \@minus -.2ex}%
   {2.3ex \@plus.2ex}%
   {\normalfont\LARGE\bfseries}
}

\def\chaptermark#1{}
