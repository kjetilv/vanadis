/*
 * Copyright 2008 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.vanadis.lang.piji;

class ExpressionCheck {

    static boolean isList(Expression expr) {
        return expr instanceof ListNode;
    }

    static boolean isLeaf(Expression expr) {
        return expr instanceof LeafNode;
    }

    static boolean isSymbol(Expression expr) {
        return isLeaf(expr) && ((LeafNode) expr).isContentSymbol();
    }

    static boolean isString(Expression expr) {
        return isLeaf(expr) && ((LeafNode) expr).isContentString();
    }

    static LeafNode checkLeaf(Object obj, Expression expr)
        throws BadArgumentException {
        return checkLeaf(obj, expr, "Must be leaf");
    }

    static LeafNode checkLeaf(Object obj, Expression expr, String msg)
        throws BadArgumentException {
        if (isLeaf(expr)) {
            return (LeafNode) expr;
        }
        throw new BadArgumentException
            (obj + " got non-leaf " + expr + ": " + msg);
    }

    static String checkString(Object obj, Expression expr)
        throws BadArgumentException {
        return checkString(obj, expr, "Must be string");
    }

    static String checkString(Object obj, Expression expr, String msg)
        throws BadArgumentException {
        if (isString(expr)) {
            return ((LeafNode) expr).getStringContent();
        }
        throw new BadArgumentException
            (obj + " got non-string " + expr + ": " + msg);
    }

    static Symbol checkSymbol(Object obj, Expression expr)
        throws BadArgumentException {
        return checkSymbol(obj, expr, "Must be symbol", true);
    }

    static Symbol checkSymbol(Object obj, Expression expr, boolean fail)
        throws BadArgumentException {
        return checkSymbol(obj, expr, null, fail);
    }

    static Symbol checkSymbol(Object obj, Expression expr, String msg)
        throws BadArgumentException {
        return checkSymbol(obj, expr, msg, true);
    }

    static Symbol checkSymbol(Object obj, Expression expr,
                              String msg, boolean fail)
        throws BadArgumentException {
        if (isSymbol(expr)) {
            return ((LeafNode) expr).getSymbolContent();
        }
        if (fail) {
            throw new BadArgumentException
                (obj + " got non-symbol " + expr + ": " + msg);
        }
        return null;
    }

    static ListNode checkList(Object obj, Expression expr)
        throws BadArgumentException {
        return checkList(obj, expr, "Must be list");
    }

    static ListNode checkList(Object obj, Expression expr, String msg)
        throws BadArgumentException {
        if (isList(expr)) {
            return (ListNode) expr;
        }
        throw new BadArgumentException
            (obj + " got non-list " + expr + ": " + msg);
    }

}
