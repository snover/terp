## Introduction

TERp is an automatic evaluation metric for Machine Translation, which takes as input a set of reference translations, and a set of machine translation output for that same data. It aligns the MT output to the reference translations, and measures the number of 'edits' needed to transform the MT output into the reference translation. TERp is an extension of TER (Translation Edit Rate) that utilizes phrasal substitutions (using automatically generated paraphrases), stemming, synonyms, relaxed shifting constraints and other improvements. TERp is named after the University of Maryland mascot: the Terrapin, so it's pronounced "terp".

For a technical description of TERp, please refer to `doc/terp_description.pdf`.

## Installation and Setup

These instructions are for use on a UNIX-like operating system.  

1. TERp requires Java version 1.5.0 or higher.

2. Build TERp by running `ant clean; ant` in the root of the repository.

3. Download and install [WordNet version 3.0](http://wordnet.princeton.edu/wordnet/download/current-version/). (**Note**: if you are on OS X, and are using macports, you can simply do `sudo port install wordnet`.)

4. Download the compressed paraphrase table (`unfiltered_phrasetable.txt.gz`) from the GitHub releases page to the `data` directory and uncompress it.

5. Several shell scripts are provided to simplify the process of running TERp.  To setup these scripts run:

    ```bash
    bin/setup_bin.sh <PATH_TO_TERP> <PATH_TO_JAVA> <PATH_TO_WORDNET>
    ```
     
     where:

    - `<PATH_TO_TERP>` points to the directory where you checked out this repository, such that `<PATH_TO_TERP>/bin/setup_bin.sh` exists. 

    - `<PATH_TO_JAVA>` points to the root of the Java 1.5.0+ directory such
       that `<PATH_TO_JAVA>/bin/java` exists.
           
    - `<PATH_TO_WORDNET>` points to the root of the WordNet 3 installation such that `<PATH_TO_WORDNET>/dict` exists. (**Note**: if you are on OS X, and you installed wordnet using macports with default options, you can set this to `/opt/local/share/WordNet-3.0`).

    Running this script will create the following additional wrapper scripts:
    
    - `bin/terp`
    - `bin/terpa`
    - `bin/terp_ter`
    - `bin/tercom`
    - `bin/create_phrasedb`
    - `bin/optimize_db`

    and create the parameter file:

    - `data/data_loc.param`

6. Generate a TERp compatible paraphrase table from the text-based paraphrase file you downloaded in Step 4 by running: 

    ```bash
    bin/create_phrasedb data/unfiltered_phrasetable.txt data/phrases.db
    ```

    **IMPORTANT**: 
   
    This step could take a while and will require several gigabytes of diskspace, as the text version of the phrase table is converted to a Berkley style database.  The conversion tool also expects to have 1-3 GBs of memory available.  This requirement can be reduced if necessary in the bin/create_phrasedb script. 

    This step will generate a phrase table database in `data/phrases.db` and will only need to be run once. Running this step again will add to the existing database, not overwrite it.

    The paraphrases used in this database were extracted using the pivot-based method (Bannard and Callison-Burch, 2005) with several additional filtering mechanisms to increase precision.  The corpus used for extraction was an Arabic-English newswire bitext containing approximately 1 million sentences.

7. You can run some validation experiments to test the installation. From the root of the repository, run:

    ```bash
    mkdir -p test/output
    ./bin/create_phrasedb test/sample.pt.txt test/sample.pt.db
    ./bin/terpa test/sample.terp.param
    ```

    This will create a small phrase database from the file `test/sample.pt.txt` and store that database as `test/sample.pt.db`. We will use this sample database for our test since using the full database will be slower.

    TERpA will then be run on the hypothesis and reference files in `test/` with the output placed in `test/output/` as specified in the `test/sample.terp.param` parameter file.  The correct version of these output files is provided in `test/correct_output/`.

    Running the three commands above should yield the following output (with appropriate substitutions for local file paths):

    ```bash
    $> mkdir -p test/output

    $> ./bin/create_phrasedb test/sample.pt.txt test/sample.pt.db
    Converting Phrase Table from test/sample.pt.txt
    Storing Database in test/sample.pt.db
    Done adding phrases to test/sample.pt.db

    $> ./bin/terpa test/sample.terp.param
    Loading parameters from /Users/nmadnani/work/terp/data/terpa.param
    Loading parameters from /Users/nmadnani/work/terp/data/data_loc.param
    Loading test/sample.terp.param as parameter file
    "test/sample.hyp.sgm" was successfully parsed as XML
    "test/sample.ref.sgm" was successfully parsed as XML
    Creating Segment Phrase Tables From DB
    Processing [ihned.cz/2008/09/29/36559][0001]
    Processing [ihned.cz/2008/09/29/36559][0002]
    Processing [ihned.cz/2008/09/29/36559][0003]
    Processing [ihned.cz/2008/09/30/36776][0001]
    Processing [ihned.cz/2008/09/30/36776][0002]
    Processing [ihned.cz/2008/09/30/36776][0003]
    Processing [ihned.cz/2008/09/30/36776][0004]
    Processing [ihned.cz/2008/09/30/36776][0005]
    Processing [ihned.cz/2008/09/30/36776][0006]
    Finished Calculating TERp
    Total TER: 0.48 (91.13 / 188.00)
    ```

## Usage

The following scripts provide easy access to the TERp program, and
serve as wrappers around java and the default parameter files.

- `bin/create_phrasedb` - This script converts a text format phrase
table to a Berkeley style database that allows for fast searching of
the phrase table at run time.

- `bin/optimize_terp` - This script allows the optimization of the edit
costs of TERp to maximize correlation with reference judgments.  See
the online documentation for more details on its use.

- `bin/tercom` - This script run the original, non-TERp, version of TER
0.7.25.  It is not supported as part of this codebase, and its usage
is no documented here (although it is essentially the same as version
0.7.25 of TERcom).

- `bin/terp` - This script is the most basic TERp wrapper and runs TERp
with default parameters only.

- `bin/terp_ter` - This script runs TERp with the parameters of TER,
turning off stemming, synonymy, phrase substitutions and using the
edit costs from TER.  Due to changes in the shift search order,
results may differ from the TERcom program.

- `bin/terpa` - This script runs TERp with parameters that were tuned
as part of the NIST Metrics MATR 2008 Challenge.  This TERp-A metric
was optimized for Adequacy on a subset of the MT06 dataset that was
annotated and distributed by NIST as a development dataset as part of
the Challenge.

### Basic Usage

1. `terp`, `terpa` and `terp_ter` can be run in an identical manner, so `terp` will be used for the following examples.

    All three programs require at least a reference file and a hypothesis (the MT output) file to score.  These files can be in SGML, XML or trans format.  Both files should be in the same format.

    To run with a given reference and hypothesis:

    ```bash
    bin/terp -r <reference> -h <hypothesis>
    ```

    Options to terp can be provided either at the command-line or using parameter files (or a combination of these).  Due to the large number of options available when running TERp, many options can only be specified using parameter files.  Parameter files contain a series of lines, each containing a parameter name and its value.  Command-line options are overridden by parameter files, and options in later parameter files are used over those in earlier parameter files.

    Any arguments given to TERp that are not command-line flags or their arguments will be treated as parameter filenames. The reference and hypothesis file can also be specified in parameter files, so that terp could be run:

    ```bash
    bin/terp <param-file>
    ```

    where param-file is:

    ```text
    Reference File (filename):  <reference>
    Hypothesis File (filename): <hypothesis>
    ```

    Running TERp in this manner provides minimal output.  Running it with
    the following options will give additional scoring that may prove
    useful.

    ```bash
    bin/terp -r <reference> -h <hypothesis> -o sum,pra,nist,html,param
    ```

    This will cause TERp to output a summary file (`.sum`) that will list the number of times each edit occured in each segment, a human readable text file (`.pra`) containing the TERp alignment for each segment, an html version of the alignment (`.html`), as well NIST Metric MATR output (`nist`) giving system, document and segment scores for each system being scored, with the scores being scored in a series of `.scr` files.  The options used in this run of TERp are also output a parameter file (`.param`) to enable easy rerunning of scoring and logging of parameters used.  More details on output formats can be found in the online documentation.

    Running TERp with no options (or incorrect options) will cause TERp to output its command-line usage.

2. `create_phrasedb` takes a text phrase table and inserts those phrases into a Berkeley style database.

    ```bash
    bin/create_phrasedb <TEXT_FILE> <DB_FILE>
    ```

    Where `<DB_FILE>` is the directory that will contain the files of the database.  Existing databases at that location will be added to, not overwritten.  If the directory does not exist, the create_phrasedb script will create it.

    The format of the phrase table text format is shown below. Each line in the text file can be must be of the form (incorrect entries or blank entries are currently silently ignored):

    ```text
    COST <p>PHRASE_1</p> <p>PHRASE_2</p>
    or
    COST_1 COST_2 <p>PHRASE_1</p> <p>PHRASE_2</p>
    ```

    indicates that `PHRASE_1` in the reference can be substituted with an edit cost of COST with `PHRASE_2` in the hypothesis.  If phrase table adjustment functions are used (as is the case in TERp-A), then it can be desirable to have be the probability of `PHRASE_1` being a paraphrase of `PHRASE_2`.  This paraphrase is only allowed in one direction: i.e., "car on fire" in the reference is not considered a paraphrase of "ablaze car".

    For example, the following line:

    ```
    0.15 <p>ablaze car</p> <p>car on fire</p>
    ```

    indicates that "ablaze car" in the reference is a paraphrase of "car on fire" with cost or probability 0.15.

    ```
    COST_1 COST_2 <p>PHRASE_1</p> <p>PHRASE_2</p>
    ```

    is equivalent to the following two lines (and is thus just a notional shortcut):

    ```
    COST_1 <p>PHRASE_1</p> <p>PHRASE_2</p>
    COST_2 <p>PHRASE_2</p> <p>PHRASE_1</p>
    ```

    For example, the following line in the phrase table:

    ```
    0.15 0.6 <p>ablaze car</p> <p>car on fire</p>
    ```

    is the same as having the following two lines:

    ```
    0.15 <p>ablaze car</p> <p>car on fire</p>
    0.6 <p>car on fire</p> <p>ablaze car</p>
    ```

If either phrase is blank (e.g., `<p> </p>` for example) or if the two phrases are identical, the paraphrase will not be inserted into the phrase table.

## What is the TRANS format?

The TRANS format was developed specifically for translation systems and requires that the hypothesis and the reference files have one segment/sentence per line but ALSO that each line should have an id following it after a space and in parentheses. These IDs generally take the form of `[SYSTEM_NAME][DOCUMENT_ID][SEGMENT_NUMBER]`. For example, here are an example hypothesis and reference files in TRANS format.

```bash
$> cat ref.trans
i am nice ([sys][doc][1])
i am good ([sys][doc][2])
i am bad ([sys][doc][3])

$> cat hyp.trans
i am nice ([sys][doc][1])
i am good ([sys][doc][2])
i am wild ([sys][doc][3])
```

## Citing TERp

References to TERp should cite:

```
   Matthew Snover, Nitin Madnani, Bonnie Dorr, and Richard Schwartz,
   "Fluency, Adequacy, or HTER? Exploring Different Human Judgments
   with a Tunable MT Metric", Proceedings of the Fourth Workshop on
   Statistical Machine Translation at the 12th Meeting of the European
   Chapter of the Association for Computational Linguistics
   (EACL-2009), Athens, Greece, March, 2009.
```

## License

TERp is distributed under the the LGPL as described in `LICENSE.md`. 

However, TERp uses Berkeley DB Java Edition version 3.3.75, which is distributed under a separate license (see `LICENSE_DB.txt`). While the core classes of the Berkeley DB are included in the TERp release, the source code is available [here](http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/overview/index.html).

TERp also uses Brett Spell's Java API for WordNet Searching (JAWS), available [here](http://lyle.smu.edu/~tspell/jaws).
