/////////////////////////////////////////////////////////////README///////////////////////////////////////////////////////////////////////////////////////////////////////

1. First Compile the Parser.java file. This file has no additional dependencies. Use JDK 8 or higher for compilation.
2. Add the test case at the prompt.
3. Observe the output at the bottom of the input provided.

Important Information
1. Obtain the test cases from testcases.txt
2. Syntax Rules

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
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////