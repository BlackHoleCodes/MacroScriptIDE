/*
 * MacroScript grammar.
 *
 * This file is the single source of truth for the language's keywords. The editor's
 * syntax colouring (MacroScriptTokenMaker) reuses the lexer generated from this file,
 * so adding a keyword here is enough to make the editor colour it.
 *
 * The parser rules only describe the shape of the code. Meaning (which commands exist,
 * what arguments they take, which types are compatible) is checked by the compiler.
 */
grammar MacroScript;

// =====================================================================================
// Parser rules
// =====================================================================================

file
    : annotation* topLevelBlock* EOF
    ;

annotation
    : ANNOTATION ( '(' expression? ')' )?
    ;

topLevelBlock
    : VARIABLES '{' declaration* '}'                   # variablesBlock
    | TRIGGERS '{' triggerStatement* '}'               # triggersBlock
    | CONSTRAINTS '{' constraintStatement* '}'         # constraintsBlock
    | ACTIONS '{' statement* '}'                       # actionsBlock
    ;

// ----- variables block ---------------------------------------------------------------

declaration
    : modifier* typeName declarator ( ',' declarator )* ';'
    ;

modifier
    : GLOBAL | SECURE | IN | OUT
    ;

typeName
    : BOOL | INT | STRING | DECIMAL | DICT | ARRAY | COLOR
    ;

declarator
    : IDENT ( '=' expression )?
    ;

// ----- triggers and constraints blocks -----------------------------------------------

triggerStatement
    : annotation* call ( AS IDENT )? whenClause? ';'
    ;

constraintStatement
    : annotation* expression ';'
    ;

whenClause
    : WHEN '(' expression ')'
    ;

// ----- actions block -----------------------------------------------------------------

statement
    : annotation* statementBody
    ;

statementBody
    : call whenClause? ';'                                         # callStatement
    | assignment ';'                                               # assignmentStatement
    | IF '(' expression ')' body elseIfClause* elseClause?         # ifStatement
    | WHILE '(' expression ')' body                                # whileStatement
    | DO body WHILE '(' expression ')' ';'                         # doWhileStatement
    | REPEAT '(' expression ')' body                               # repeatStatement
    | FOR '(' IDENT ',' IDENT ':' expression ')' body              # forStatement
    | BREAK ';'                                                    # breakStatement
    | CONTINUE ';'                                                 # continueStatement
    | RETURN ';'                                                   # returnStatement
    ;

elseIfClause
    : ELSE IF '(' expression ')' body
    ;

elseClause
    : ELSE body
    ;

body
    : '{' statement* '}'
    ;

assignment
    : assignTarget op=( '=' | '+=' | '-=' | '*=' | '/=' | '%=' ) expression
    | assignTarget op=( '++' | '--' )
    ;

assignTarget
    : IDENT ( '[' expression ']' )*
    ;

// ----- calls and expressions ---------------------------------------------------------

call
    : IDENT '(' arguments? ')'
    ;

arguments
    : argument ( ',' argument )* ','?
    ;

argument
    : ( name ':' )? expression
    ;

expression
    : primary                                                      # primaryExpression
    | expression '[' expression ']'                                # indexExpression
    | expression '.' IDENT '(' arguments? ')'                      # methodCallExpression
    | '(' typeName ')' expression                                  # castExpression
    | op=( '!' | '-' ) expression                                  # unaryExpression
    | expression op=( '*' | '/' | '%' ) expression                 # binaryExpression
    | expression op=( '+' | '-' ) expression                       # binaryExpression
    | expression op=( '<' | '>' | '<=' | '>=' ) expression         # binaryExpression
    | expression op=( '==' | '!=' ) expression                     # binaryExpression
    | expression op='&&' expression                                # binaryExpression
    | expression op='||' expression                                # binaryExpression
    ;

primary
    : literal                                                      # literalPrimary
    | call                                                         # callPrimary
    | SYS '.' IDENT                                                # sysPrimary
    | IDENT                                                        # namePrimary
    | '(' expression ')'                                           # parenPrimary
    | '[' ( expression ( ',' expression )* ','? )? ']'             # listPrimary
    | '{' ( objectEntry ( ',' objectEntry )* ','? )? '}'           # objectPrimary
    ;

objectEntry
    : ( name | STRING_LITERAL ) ':' expression
    ;

literal
    : INT_LITERAL
    | DECIMAL_LITERAL
    | STRING_LITERAL
    | DURATION_LITERAL
    | COLOR_LITERAL
    | TRUE
    | FALSE
    | NULL
    ;

/** A name in argument and object key position. Keywords are permitted here. */
name
    : IDENT
    | VARIABLES | TRIGGERS | CONSTRAINTS | ACTIONS
    | IF | ELSE | WHILE | DO | REPEAT | FOR | BREAK | CONTINUE | RETURN
    | TRUE | FALSE | NULL | AS | WHEN
    | GLOBAL | SECURE | IN | OUT
    | BOOL | INT | STRING | DECIMAL | DICT | ARRAY | COLOR | SYS
    ;

/** Entry point used to parse the text inside ${...} in a string literal. */
interpolation
    : expression EOF
    ;

// =====================================================================================
// Lexer rules
// =====================================================================================

// ----- keywords (the reserved words of the language) ---------------------------------

VARIABLES   : 'variables' ;
TRIGGERS    : 'triggers' ;
CONSTRAINTS : 'constraints' ;
ACTIONS     : 'actions' ;
IF          : 'if' ;
ELSE        : 'else' ;
WHILE       : 'while' ;
DO          : 'do' ;
REPEAT      : 'repeat' ;
FOR         : 'for' ;
BREAK       : 'break' ;
CONTINUE    : 'continue' ;
RETURN      : 'return' ;
TRUE        : 'true' ;
FALSE       : 'false' ;
NULL        : 'null' ;
AS          : 'as' ;
WHEN        : 'when' ;
GLOBAL      : 'global' ;
SECURE      : 'secure' ;
IN          : 'in' ;
OUT         : 'out' ;
BOOL        : 'bool' ;
INT         : 'int' ;
STRING      : 'string' ;
DECIMAL     : 'decimal' ;
DICT        : 'dict' ;
ARRAY       : 'array' ;
COLOR       : 'color' ;
SYS         : 'sys' ;

// ----- literals ----------------------------------------------------------------------

ANNOTATION       : '@' [a-zA-Z_] [a-zA-Z0-9_]* ;
DURATION_LITERAL : ( DIGITS ( 'ms' | 'h' | 'm' | 's' ) )+ ;
DECIMAL_LITERAL  : DIGITS '.' DIGITS ;
INT_LITERAL      : DIGITS ;
COLOR_LITERAL    : '#' HEX HEX HEX HEX HEX HEX ( HEX HEX )? ;
STRING_LITERAL   : '"' STRING_CHAR* '"' ;
IDENT            : [a-zA-Z_] [a-zA-Z0-9_]* ;

// ----- operators and separators ------------------------------------------------------

LPAREN   : '(' ;
RPAREN   : ')' ;
LBRACE   : '{' ;
RBRACE   : '}' ;
LBRACKET : '[' ;
RBRACKET : ']' ;
SEMI     : ';' ;
COMMA    : ',' ;
DOT      : '.' ;
COLON    : ':' ;

ASSIGN     : '=' ;
ADD_ASSIGN : '+=' ;
SUB_ASSIGN : '-=' ;
MUL_ASSIGN : '*=' ;
DIV_ASSIGN : '/=' ;
MOD_ASSIGN : '%=' ;
INCREMENT  : '++' ;
DECREMENT  : '--' ;

EQ  : '==' ;
NEQ : '!=' ;
LE  : '<=' ;
GE  : '>=' ;
LT  : '<' ;
GT  : '>' ;
AND : '&&' ;
OR  : '||' ;
NOT : '!' ;

PLUS  : '+' ;
MINUS : '-' ;
STAR  : '*' ;
SLASH : '/' ;
PERCENT : '%' ;

// ----- comments and whitespace -------------------------------------------------------

/** A documentation comment. The compiler copies its text to m_comment of the next item. */
DOC_COMMENT   : '/**' .*? '*/' -> channel(2) ;
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT  : '//' ~[\r\n]* -> channel(HIDDEN) ;
WHITESPACE    : [ \t\r\n\f]+ -> channel(HIDDEN) ;

// ----- error tokens ------------------------------------------------------------------
// These rules only match text that the rules above cannot. They keep the lexer from
// failing, so the parser can report a clear message and the editor can colour the rest.

/** A block comment with no closing star-slash. It runs to the end of the input. */
UNTERMINATED_COMMENT : '/*' ( ~'*' | '*'+ ~[*/] )* '*'* ;
/** A string with no closing quote. It runs to the end of the line. */
UNTERMINATED_STRING  : '"' STRING_CHAR* '\\'? ;
ERROR_CHARACTER      : . ;

fragment DIGITS : [0-9]+ ;
fragment HEX    : [0-9a-fA-F] ;
fragment ESCAPE : '\\' . ;
/** One character of a string, or a whole ${...} part. A ${...} part can hold quotes: "${d["key"]}". */
fragment STRING_CHAR   : ESCAPE | INTERPOLATION | ~["\\\r\n] ;
fragment INTERPOLATION : '${' ( '"' ( ESCAPE | ~["\\\r\n] )* '"' | ~[}"\r\n] )* '}' ;
