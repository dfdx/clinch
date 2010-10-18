
Clinch
======

Clinch is a Clojure library for semantic classification and clusterization.


Overview and Motivation
-----------------------

Clinch implements one of [VSM](http://en.wikipedia.org/wiki/Vector_space_model) high-performance algorithms called [Random Indexing](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.96.2230&rep=rep1&type=pdf). In contrast to it's traditional counterparts like [LSA/LSI](http://en.wikipedia.org/wiki/Latent_semantic_indexing), it is completely incremental and extremely efficient in both time and memory.


Getting Started
---------------

After you get the code, you will need to pull all dependencies. Clich is using [Leiningen](http://github.com/technomancy/leiningen), so just cd to the Clinch's folder and execure:

    $ lein deps

Currently there's one dependency which is not included into Leiningen project summary. This is [modified Clucy](http://github.com/faithlessfriend/clucy) library. Download it, cd to the Clucy dir and execute `lein jar` to get clucy.jar. Then just copy this new file to clinch/lib.


....


Copyright (c) Andrei Zhabinski 
Distributed under the [MIT Licence](http://www.opensource.org/licenses/mit-license.php).
