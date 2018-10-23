/////////////////////////////////////////////////////////////README///////////////////////////////////////////////////////////////////////////////////////////////////////

1. First Compile the Parser.java file. This file has no additional dependencies. Use JDK 8 or higher for compilation.
2. Make sure that the file `TestCases.txt` is present in the same folder as the java file.
3. Observe the output inside `Output.txt` in the same folder

Important Information
1. Obtain the test cases from TestCases.txt.
2. The output of these testcases are placed in Output.txt
3. Syntax Rules

Variables
var identifier:type
type is primitive or previously defined struct

Arrays
var identifier:[type]
more number of '[]' represent higher dimensional arrays
[[type]] 2D array
and
[[[type]]] 3D array

Structures
struct identifier{
	var identifier:array_type or primitive_type
	.
	.
	repeated as many times as required
}

Functions
func identifier(arg1:argType1,arg2:argType2...){}
Here the return type is optional
func identifier(arg1:argType1,arg2:argType2...)->identifier:type{}
For multiple return args
func identifier(arg1:argType1,arg2:argType2...)->(identifier1:type1,identifier2:type2,..){}

Important: Only one argument in the paranthesis for the return type is not allowed

****************************************CRITICAL*********************************************************************************************
Swift does not support internal name equivalence, but we have added support to demonstrate the working of the concept.
example:
a,b,c:Int
All 3 will be of type int
Note: We do not use `var` at the start.

Donot have multiple variables with the same name, checking for that is not present in this code

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ FLOW OF THE PROGRAM @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

1. The input is taken from a file `TestCases.txt` [camel case]
2. File should start with the word `TESTCASE`
4. Every test case should be separated by the keyword `TESTCASE`
5. We parse through every line and determine the class: FUNCTION, STRUCTURE, ARRAY
6. We then define functions to account for the differnt classes defined within the program
7. Each function adds an identifier to the identifier table
8. Each functions arguments to the non-primitive hashMap and the return types to the returnMap
9. The structures add the embeded types to the non-primitive hashMap
10. We then run the Equivalnece class to find the internal name equivalence, name equivalence, and structural equivalence

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

*****************************************TEST CASES*****************************************************************************************
1.
Input:
var a: Int
var b: Int
var c: String
var d: Float

struct s{
	var x: [Int]
	var y: String
}

struct t{
	var xx: String
	var yy: [Int]
}

Output:
@@@@@@@@@@@@@@@@@@ PRIMITIVE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Float: d
String: c
Int: a b
@@@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ STRUCTURE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
s: s
t: t
@@@@@@@@@@@@@@@@@@ FUNCTION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

Explanation:
`s` and `t` may have the same variable types but since their positioning is different, they are not structurally equivalent and hence are structurally equivalent to themselves. `a` and `b` have the same type int, therefore they are name equivalent, while `c` and `d` are not.

2.
var a: [[Int]]
b,c,d: [[Float]]
struct s{
	var p:String
	var e:[[Float]]
}
func f1(arg1:s,arg2:[s],arg3:Float)->rarg1:String{}
func f2(arg6:s,arg5:[s],arg4:Float)->rarg2:String{}
func f3(arg7:[s],arg8:Float)->rarg3:String{}
func f4(arg9:[s],arg10:s,arg11:[[s]])->rarg:String{}

Output:
@@@@@@@@@@@@@@@@@@ INTERNAL NAME EQ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1. Array of dimension 2 and type is Float: b c d
@@@@@@@@@@@@@@@@@@ PRIMITIVE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Int array:2 2: a
@@@@@@@@@@@@@@@@@@ STRUCTURE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
s: s
@@@@@@@@@@@@@@@@@@ FUNCTION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
f1: f1 f2
f3: f3
f4: f4

Explanation: According to the explanation given in the CRITICAL section, `b`, `c`, `d` are considered internally name equivalent. `a` is an Int array of 2 dimensions. `s` is structurally equivalent to `s`, obviously. Functions f1, f2 are structurally equivalent because their arguments at every place are structurally equivalent and they have the same return type and the number of arguments. `f3` has 2 arguments, as compared to 3 in `f1`, `f2`, `f4`. `f4` is not structurally equivalent to either `f1` or `f2` because its arguments are positioned differently than any of those.

3.
Input:
struct s{
	var p: String
	var q: Float
}
struct t{
	var a: String
	var b: Float
}
struct r{
	var m: Float
	var n: String
}
func f1(arg1:s)->(a1:String,b1:Int){}
func f2(arg2:t)->(a2:String,b2:Int){}

Output:

@@@@@@@@@@@@@@@@@@ PRIMITIVE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ STRUCTURE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
r: r
s: s t
@@@@@@@@@@@@@@@@@@ FUNCTION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
f1: f1 f2

Explanation:
`s` and `t` are structurally equivalent because their elements at each position are structurally equivalent, it is not the case with `r`. Function `f1` and `f2` use just one argument, and they are structurally equivalent as proved above, therefore since their return types match, they are structurally equivalent

4.

Input:
func f(){}
func g(){}

Ouput:
@@@@@@@@@@@@@@@@@@ PRIMITIVE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ STRUCTURE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@ FUNCTION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
f: f g

Explanation: These are functions without an argument or return type (already optional, as stated above), so they must be structurally equivalent
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
