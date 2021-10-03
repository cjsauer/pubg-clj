# pubg-clj

This library enables idiomatic access to the [PUBG API][1] using the Clojure
programming language, as well as [specs][2] and a [Datascript][3] schema for
a large portion of the API, allowing for more powerful data mining.

## Installation

Add this project as a git dependency to your `deps.edn` file:

```Clojure
{:deps
 {cjsauer/pubg-clj {:git/url "https://github.com/cjsauer/pubg-clj"
                    :sha     "6a9c098cdce80fe3322f55aabb0f3fd2d922b77a"}}}
```

(See [this guide][4] for reference.)

## Usage

First require the API namespace:

```Clojure
(require '[pubg-clj.api :as pubg])
```

To actually use the API, you'll need to [obtain your own API key][1]. All
remote calls should then be wrapped in the `with-api-key` macro:

```Clojure
(pubg/with-api-key "MY-API-KEY"
  (pubg/fetch ...)
  (pubg/fetch ...)
  ...)
```

**If you do not wrap your remote calls in this macro, you will receive an
exception!**

## Sample

There are helper functions for most domain entities in the API. Here is an
example of fetching a specific player by their platform and name:

```Clojure
(def player
  (pubg/with-api-key "MY-API-KEY"
    (pubg/fetch-player-by-name "pc" "shroud")))
```

which results in:

```
{:pubg.player/id "account.d50fdc18fcad49c691d38466bed6f8fd",
 :pubg.player/name "shroud",
 :pubg/shard-id "steam",
 :pubg.player/patch-version "",
 :pubg/title-id "bluehole-pubg",
 :pubg.player/matches
 [#:pubg.match{:id "88057446-0f90-4740-92f1-4382ea92136c"}
  #:pubg.match{:id "410998d7-dede-4f0c-9203-99223a91ddda"}
  ...]}
```

Notice that responses are expressed in idiomatic Clojure maps. These responses
are spec'd, mostly for internal testing, but you may find uses for them in your
own projects:

```Clojure
(spec/valid? :pubg/player player)
;; true
```

You can find the exhaustive spec in the [pubg-clj.api.omni][5] namespace. This
namespace houses a sort of "master specification", from which not only can we
generate specs, but also a [Datascript][3] schema. This allows for some very
powerful query ability on result sets.

(Note that you must add [Datascript][3] to your `deps.edn`/`project.clj` file
yourself. It is not provided by this project.)

Here is an example:

```Clojure
(require '[datascript.core :as d]
         '[pubg-clj.omnigen :as o]
         '[pubg-clj.api.omni :refer [pubg-omni]])
         
(def conn
  (let [schema (o/datascript-schema pubg-omni)]
    (d/create-conn schema)))
```

We now have a Datascript connection that is ready to ingest PUBG API data!
We did this by generating a schema from our "master specification". Let's
try it out by transacting our fetched player against the connection:

```Clojure
(d/transact! conn [player])
```

Great! Let's try running a query for all player names that we've fetched
thus far:

```Clojure
(d/q '[:find [?name ...]
       :where
       [?e :pubg.player/name ?name]]
     @conn)
;; => ["shroud"]
```

Success! Of course this is much more interesting if we load up some more data.
Let's try fetching Shroud's latest matches, transacting that against our
connection, and totaling his kills:

```Clojure
(def matches
  (pubg/with-api-key my-api-key
    (pubg/fetch-player-matches player)))
    
(count matches)
;; => 64
    
(d/transact! conn matches)

(d/q '[:find (sum ?kills) .
       :where
       [?p :pubg.match.participant/name "shroud"]
       [?p :pubg.match.participant.stats/kills ?kills]]
     @conn)
;; => 87
```

Obviously you can parameterize your queries to make them more expressive,
but this should expose the general idea.

## API Reference

You should consult the [official API documentation][1] to get a general overview
of what the API is capable of. For this client library, consult the
[pubg-clj.api][6] namespace for all top-level functionality. To understand the
general "shape" of what is returned by this client library, consult the
[omni.cljc][5] file in this repository. It will give you not only the spec, but
also the relationships between entities that will help with forming Datascript
queries. Again, using Datascript is totally optional, and you will need to
depend on that library yourself; it is not provided by this library.

## Development

The samples in this readme are contained in the `dev/user.clj` file for local testing.
You should paste your API key into the `api_key.txt` file in order to run these
successfully, as well as for generating fixtures (see next paragraph).

There are a handful of tests for validating parsing of domain entities. These
tests read from the `fixtures.edn` file in order to avoid making real network
calls. In order to generate a fresh `fixtures.edn` file, use the
`pubg-clj.api-test/generate-fixtures` function. Calling this function will
request various sample data from the API _with parsing disabled_, and drop the
pretty-printed results into the `fixtures.edn` file ready for parser testing.

To run the tests from the command line:

```
clojure -A:test
```

If you're using Cider, you can start a development REPL using the
`./bin/cider-nrepl` shell script. The command contained within is identical to
what `cider-jack-in` would normally start, but with the `-A:dev:test` aliases
added for pulling in a few development dependencies (Datascript included).

### Bugs

Please feel free to open pull requests with additional features, or new issues
if you happen to find problems (I'm sure there are a few).

## License

Copyright © 2019 Calvin Sauer

Licensed under MIT License (see [LICENSE][7]).

[1]: https://developer.playbattlegrounds.com
[2]: https://clojure.org/guides/spec
[3]: https://github.com/tonsky/datascript
[4]: https://clojure.org/guides/deps_and_cli
[5]: ./src/pubg_clj/api/omni.cljc
[6]: ./src/pubg_clj/api.clj
[7]: ./LICENSE
