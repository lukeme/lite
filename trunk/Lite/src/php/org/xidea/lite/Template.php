#!/usr/bin/env python
# -*- coding: utf-8 -*-

from Expression import evaluate

define('EL_TYPE',0);// [0,'el']
define('IF_TYPE',1);// [1,[...],'test']
define('BREAK_TYPE',2);// [2,depth]
define('XML_ATTRIBUTE_TYPE',3);// [3,'value','name']
define('XML_TEXT_TYPE',4);// [4,'el']
define('FOR_TYPE',5);// [5,[...],'var','items','status']#status
define('ELSE_TYPE',6);// [6,[...],'test']#test opt?
define('ADD_ONS_TYPE',7);// [7,[...],'var']
define('VAR_TYPE',8);// [8,'value','name']
define('CAPTRUE_TYPE',9);// [9,[...],'var']

define('FOR_KEY', "for");
define('IF_KEY', "if");

class Template{
    function Template($items){
        $this->items = $items;
    }
    function render($context,$out){
        renderList($context, $this->items, $out);
    }
}
    
function renderList($context, $children, $out){
    foreach($children as $item){
        try{
            if(item instanceof string){
                out.write(item)
            }else:
                if item[0] == EL_TYPE:
                    processExpression(context, item, out, False)
                elif item[0] == XML_TEXT_TYPE:
                    processExpression(context, item, out, True)
                elif item[0] == VAR_TYPE:
                    processVar(context, item)
                elif item[0] == CAPTRUE_TYPE:
                    processCaptrue(context, item)
                elif item[0] == IF_TYPE:
                    processIf(context, item, out)
                elif item[0] == ELSE_TYPE:
                    processElse(context, item, out)
                elif item[0] == FOR_TYPE:
                    processFor(context, item, out)
                elif item[0] == XML_ATTRIBUTE_TYPE:
                    processAttribute(context, item, out)
        except (Exception, ), e:
            print "aaaaa" 
            print item[0]
            print e
            pass

def printXMLAttribute(text, context, out, escapeSingleChar):
    ## for-while
    for c in text:
        if c == '<':
            out.write("&lt;")
            break
        elif c == '>':
            out.write("&gt;")
            break
        elif c == '&':
            out.write("&amp;")
            break
        elif c == '"':
            out.write("&#34;")
            break
        elif c == '\'':
            if escapeSingleChar:
                out.write("&#39;")
            break
        else:
            out.write(c)

def printXMLText(text, out):
    ## for-while
    for c in text:
        if c == '<':
            out.write("&lt;")
            break
        elif c == '>':
            out.write("&gt;")
            break
        elif c == '&':
            out.write("&amp;")
            break
        else:
            out.write(c)

def toBoolean(test):
    if test is None:
        return False
    else:
        if isinstance(test, list) or isinstance(test, dict):
            return true
        elif test:
            return True
        else:
            return False
    return True

def processExpression(context, data, out, encodeXML):
    value = evaluate(data[1],context)
    if encodeXML and value is not None:
        printXMLText(str(value), out)
    else:
        out.write(str(value))

def processIf(context, data, out):
    test = True
    try:
        if toBoolean(evaluate(data[2],context)):
            renderList(context, data[1], out)
        else:
            test = True
    finally:
        test = True
    context[IF_KEY]=test

def processElse(context, data, out):
    if not toBoolean(context[IF_KEY]):
        try:
            if data[2] is None or toBoolean(evaluate(data[2],context)):
                renderList(context, data[1], out)
                context[IF_KEY] = Boolean.TRUE
        except (Exception, ), e:
            if log.isDebugEnabled():
                log.debug(e)
            context[IF_KEY] = Boolean.TRUE

def processFor(context, data, out):
    children = data[1]
    varName = data[2]
    statusName = data[4]
    items = evaluate(data[3],context)
    length = len(items)
    preiousStatus = hasattr(context,FOR_KEY) and context[FOR_KEY]
    try:
        
        forStatus = ForStatus(length)
        context[FOR_KEY]=forStatus
        if statusName is not None:
            context[statusName]=forStatus
        for item in items:
            forStatus.index += 1
            context[varName]=item
            renderList(context, children, out)
        if statusName is not None:
            context[statusName]=preiousStatus
    finally:
        context[FOR_KEY]=preiousStatus
        context[IF_KEY]= length > 0

def processVar(context, data):
    context.put(data[2], evaluate(data[1],context))

def processCaptrue(context, data):
    buf = StringWriter()
    renderList(context, data[1], buf)
    context.put(data[2], str(buf))

def processAttribute(context, data, out):
    result = evaluate(data[1],context)
    if data[2] is None:
        printXMLAttribute(str(result), context, out, True)
    elif result is not None:
        out.write(" ")
        out.write(data[2])
        out.write("=\"")
        printXMLAttribute(str(result), context, out, False)
        out.write('"')

class ForStatus(object):
    """ generated source for ForStatus

    """
    index = -1
    lastIndex = 0

    def __init__(self, end):
        self.lastIndex = end - 1