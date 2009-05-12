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

package vanadis.lang.piji.fun;

/**
 * @author kjetil $Id: Functions.java,v 1.2 2004/05/26 06:51:20 kjetil Exp $ $Revision: 1.2 $ $Author: kjetil $ $Date: 2004/05/26 06:51:20 $
 */
public class Functions {

    public static final String[][] MAP = new String[][]
        {{"LambdaFunction", "lambda"},

         {"ThreadFunction", "thread"},

         {"DocumentationFunction", "documentation"},
         {"PrintFunction", "print"},
         {"PrintlnFunction", "println"},
         {"DefineFunction", "define"},
         {"SynchronizedFunction", "synchronized"},
         {"EqualsFunction", "equals"},
         {"ThrowFunction", "throw"},
         {"TryCatchFunction", "try-catch"},
         {"SetFunction", "set!"},
         {"BeginFunction", "begin"},
         {"IncfFunction", "incf!"},
         {"IfFunction", "if"},
         {"IsNullFunction", "null=="},
         {"WhileFunction", "while"},
         {"DoFunction", "do-while"},
         {"IsBoundFunction", "bound?"},
         {"ForFunction", "for"},
         {"ForEachFunction", "for-each"},

         {"AddFunction", "+"},
         {"SubtractFunction", "-"},
         {"MultiplyFunction", "*"},
         {"DivideFunction", "/"},
         {"AndFunction", "and"},
         {"OrFunction", "or"},
         {"NotFunction", "not"},
         {"LetFunction", "let"},
         {"LetStarFunction", "let*"},

         {"NewFunction", "new"},
         {"NewPrivateFunction", "new-p"},
         {"InstanceofFunction", "instanceof"},
         {"CastFunction", "cast"},
         {"ClassFunction", "class"},

         {"NewArrayFunction", "new[]"},
         {"ArraySetFunction", "set[]"},
         {"ArrayGetFunction", "get[]"},
         {"ArrayLengthFunction", "length[]"},

         /* Proxy creation functions */
         {"NewProxyFunction", "new-proxy"},
         {"ImplementFunction", "implement"},
         {"DeclareFunction", "declare"},

         /* Returns the current context */
         {"GetContextFunction", "get-context"},

         /* Arithmetic functions */

         {"LessThanFunction", "<"},
         {"LessThanEqualFunction", "<="},
         {"SameFunction", "=="},
         {"GreaterThanFunction", ">"},
         {"GreaterThanEqualFunction", ">="},

         /* Functions that are supported through syntax as well */

         {"InvokeFunction", "invoke"},
         {"AccessFunction", "."},
         {"AccessFunction", "access"},
         {"AccessPrivateFunction", "access-p"}};

}
