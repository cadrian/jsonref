[![Build Status](https://travis-ci.org/cadrian/jsonref.svg)](https://travis-ci.org/cadrian/jsonref)

See http://cadrature.blogspot.fr/2015/06/jsonr.html

# Why a new JSON parser?

JSON is well and good, but it has one shortcoming: the management of object references. When you have to
serialize an object graph, with object cycles, JSON is lost.

My client needed to serialize such a graph. So I wrote a JSON extension, dubbed "JSON/R" (JSON with
references).

I wanted the parser to stay as pretty as the standard JSON's (see a previous article), with the same property:
a parser with no backtrack.

I added two concepts: *"heap"* and *"reference"*.

A *"heap"* is just an array with different brackets (`<` and `>`); its usage is that the objects in it are
*referenced* by their position in the array. By convention, the root object of the graph (i.e. the one the
serialization started from) is the first one, noted `$0` (see below).

A *"reference"* is an index into the heap: noted `$<integer>`.

# An example

`<{"class":"net.cadrian.jsonref.Pojo","reference":$1,"timestamp":"3915-07-10T12:00:00.000","value":"a"},{"class":"net.cadrian.jsonref.Pojo","reference":$0,"timestamp":null,"value":"b"}>`

The class is a simple pojo with a `String` *value*, a `Date` *timestamp*, and another `Pojo` *reference*. The
example above shows two objects referencing each other.
