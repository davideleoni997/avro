/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avro;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ResolverTest {

  private Schema readSchema;
  private Schema writeSchema;
  private Resolver.Action action;

  @Parameterized.Parameters
  public static Collection<Object[]> testParameters() {
    return Arrays.asList(new Object[][] { {
        SchemaBuilder.record("HandshakeRequest").namespace("org.apache.avro.ipc").fields().name("clientHash").type()
            .fixed("MD5").size(16).noDefault().name("clientProtocol").type().nullable().stringType().noDefault()
            .name("serverHash").type("MD5").noDefault().name("meta").type().nullable().map().values().bytesType()
            .noDefault().endRecord(),
        SchemaBuilder.record("HandshakeRequest").namespace("org.apache.avro.ipc").fields().name("clientHash").type()
            .fixed("MD5").size(16).noDefault().name("clientProtocol").type().nullable().stringType().noDefault()
            .name("serverHash").type("MD5").noDefault().name("meta").type().nullable().map().values().bytesType()
            .noDefault().endRecord(),
        Resolver.Action.Type.DO_NOTHING },

    });
  }

  public ResolverTest(Schema writer, Schema reader, Resolver.Action action) {
    configure(writer, reader, action);
  }

  private void configure(Schema writer, Schema reader, Resolver.Action action) {
    this.writeSchema = writer;
    this.readSchema = reader;
    this.action = action;

  }

  @Test
  public void testMethod() {
    Assert.assertEquals(action, Resolver.resolve(writeSchema, readSchema));
  }

}
