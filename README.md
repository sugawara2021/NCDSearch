# NCDSearch

A grep-like tool to find similar source code fragments using Normalized Compression Distance.

Normalized Compression Distance (https://en.wikipedia.org/wiki/Normalized_compression_distance) is defined as follows:

        NCD(x, y) = (Z(xy)-min(Z(x), Z(y)) / max(Z(x), Z(y))

where Z(x), Z(y), and Z(xy) are data size obtained by a data compression algorithm (Deflate in our implementation).
If two data `x` and `y` are similar, then NCD(x, y) results in a small value.

For example, if a line `if (this.distance < another.distance) return true;` (in source code of the tool) is given as a query, the tool reports similar lines such as 
`if (this.distance > another.distance) return false;` and `if (thislen > anotherlen) return true;`.
The tool assumes that either a long identifier or a few lines of code as a query.


## Build Information

The project uses Maven with Maven Assembly Plugin.
The following command builds a runnable jar `ncdsearch.jar`:

         mvn package

The project  includes Eclipse project file and `pom.xml` for dependencies. 
The main class is `ncdsearch.SearchMain`.
The repository includes Lexer files generated by ANTLR4.
The grammar files are maintained in another repository (https://github.com/takashi-ishio/sarf-lexer) 


## Usage

### Search a code snippet

You can input code fragments using STDIN, a query file, or command line arguments.

        java -jar ncdsearch.jar dir_or_file -lang java < query
        java -jar ncdsearch.jar dir_or_file -lang java -q query.txt
        java -jar ncdsearch.jar dir_or_file -lang java -q query.txt -sline 10 -eline 20
        java -jar ncdsearch.jar dir_or_file -lang java -e my code fragments

 - The program reads a source code fragment from STDIN by default.
 - The `-q` option specifies a file for a code fragment.  `-sline` and `-eline` specifies lines of code in a file as a query.  In the third example, lines 10 through 20 of the query.txt file are selected as a query.  
 - The `-e` option directly specifies a code fragment in command line arguments.
   Note that all the arguments after `-e` are regarded as a query code snippet.

You can specify multiple directories or files to be searched.

        java -jar ncdsearch.jar dir1 dir2 -lang java < query

The tool recognizes a programming language by either `-lang` option or a file name of `-q` option.  The `-lang` option overrides `-q` option's file name.


### Output Format

The tool reports a result in a CSV format.
For example, an execution with a query:

        java -jar ncdsearch.jar -lang java -e "if (this.distance > another.distance) return true;"

would report a line like this:

        path/to/src/ncdsearch/Fragment.java,81,81,0.10714285714285714

Each line of an output represents a similar source code fragment detected by the tool.
  * The first column is the file name including the code fragment. 
  * The second and third columns indicate the lines of the first and last tokens of the fragment. 
    * You may specify `-pos` option to extract char positions in the lines. 
  * The last column indicates the normalized compression distance between the query and the code fragment.  Since it is a distance, more similar code fragments have smaller values.

According to the report, you may find a similar line of code in a file. For example:

        81:   if (this.distance < another.distance) return true;


### Source File Encoding

The tool assumes UTF-8 by default.
Please specify `-encoding` option to choose a charset, e.g. `-encoding UTF-16`.
A list of supported encodings is dependent on a platform.  
A list for Oracle Java SE is available at: <https://docs.oracle.com/javase/jp/8/docs/technotes/guides/intl/encoding.doc.html>


### Programming Language

The `-lang` option specifies a programming language.  
The tool accepts file extensions: `java` (Java), `c` (C/C++), `cs` (C#), `js` (JavaScript), `cbl` (Cobol), and `txt` (plain text).  
It also accepts `ccfxprep` files that are generated by the CCFinderX  preprocessor. 

A programming language option activates a lexical analysis to extract tokens.  It ignores white space and comments in the specified language.
The plain text mode regards a single line as a single token but ignores indentation (leading white space and trailing white space of lines). 

Each language option automatically searches source files using the following extensions.
 - C/C++: .c, .cc, .cp, .cpp, .cx, .cxx, .c+, .c++, .h, .hh, .hxx, .h+, .h++, .hp, .hpp
 - Java: .java
 - JavaScript: .js
 - C#: .cs
 - Python: .py
 - COBOL: .cbl
 - Plain Text: .txt, .html, .md
 - CCFinderX: .ccfx, .ccfinderx, .ccfxprep

You can include additional files using `-i (extension)` option.
For example, the following command searches .java files as plain text.

        java -jar ncdsearch.jar dir_or_file -lang txt -i .java -e "// line comment"

You can use multiple `-i` options in a command line to search additional files.


### Full Scan Mode

For efficiency, the tool compares a query with sampled lines of code by default.  It is fast, but may result in false negatives.
If your query is small enough, you should specify `-full` option that checks all tokens so that you can get more results.

        java -jar ncdsearch.jar dir_or_file -lang java -full -e identifier


### Verbose Mode

If a result is different from your expectation, you can try `-v` to see the configuration and progress of the search.

        java -jar ncdsearch.jar dir_or_file -lang java -v < query

### Multi-threading

The program uses N working threads if `-thread N` option is provided.
The multi-threading execution processes N files in parallel.
File locations in the output may be differently ordered in each execution.

Although N can be an arbitrary number (e.g. 2, 4, or 8), an effective value of N is dependent on available CPU resources. 
A larger amount of memory is also required to store N files in memory at once.


### Algorithm Option

#### NCD with another compression algorithm
You can choose a compression algorithm other than Deflate.
The tool accepts `-a XZ` and `-a ZSTD` that are corresponding to Xz and Zstd algorithms.

        java -jar ncdsearch.jar dir_or_file -lang java -a XZ < query

The feature is experimental to see the dependency of compression algorithms.

#### Token-level Levenshtein Distance

The `-a ntld` option uses Normalized Levenshtein Distance on tokens, i.e. the ratio of added, removed, and modified tokens, as a distance metric.

The `-a tld` option uses Levenshtein Distance on tokens without normalization.
It simply counts the number of added, removed, and modified tokens and reports a code fragment whose distance is at most a given threshold.
For example, `-q "a < b" -th 1 -full` matches `a > b` and  `a < c`.




## License

The source code of this project is available under the MIT License.
The full license description is included in the LICENSE file.

This project uses the following components.
 - GNU Trove (https://bitbucket.org/trove4j/trove): LGPL License 
 - ANTLR4 (http://www.antlr.org/license.html): BSD License 
 - XZ (https://tukaani.org/xz/java.html): public domain 
 - ZStd (https://github.com/luben/zstd-jni): 3-clause BSD License 

Grammar files for ANTLR4 come from https://github.com/antlr/grammars-v4/ except for COBOL.
C/C++ grammars are slightly edited so that the tool can recognize macro directives as tokens (see the header comment).
COBOL grammar is obtained from ProLeap COBOL Parser (https://github.com/uwol/proleap-cobol-parser) that is distributed under MIT License.
