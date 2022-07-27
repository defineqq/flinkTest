<#--
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to you under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
-->
/**
*  USE FUNCTION func_name [ WITH (name = '', version = '') ] 自定义
*/

SqlCall SqlUseFunction() :
{
SqlParserPos pos;
SqlIdentifier funcName;
SqlNodeList funcProps = null;
}
{
<USE>
    {
    // Token位置
    pos = getPos();
    }
    <FUNCTION>
        funcName = CompoundIdentifier()
        [ <WITH> funcProps = TableProperties() ]
            {
            return new SqlUseFunction(pos, funcName, funcProps);
            }
}
