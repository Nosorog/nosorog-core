/*
 * Copyright 2016 Dmitry Telegin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosorog.core.internal;

import javax.enterprise.inject.spi.Unmanaged;
import javax.script.Bindings;

public class Binder {

    public static Bindings getBindings(Class clazz) throws InstantiationException, IllegalAccessException {

        Stub stub = (Stub) new Unmanaged(clazz)
                .newInstance()
                .produce()
                .inject()
                .postConstruct()
                .get();

        return stub.getBindings();

    }

}
