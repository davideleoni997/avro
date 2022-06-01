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

import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;

@RunWith(value = Parameterized.class)
public class ResolverTest {

  private Schema readSchema;
  private Schema writeSchema;
  private Resolver.Action.Type action;
  @Mock
  private Schema.Type type;

  @Parameterized.Parameters
  public static Collection<Object[]> testParameters() {
    return Arrays.asList(new Object[][] {

        { null,
            SchemaBuilder.record("HandshakeRequest").namespace("org.apache.avro.ipc").fields().name("clientHash").type()
                .fixed("MD5").size(16).noDefault().name("clientProtocol").type().nullable().stringType().noDefault()
                .name("serverHash").type("MD5").noDefault().name("meta").type().nullable().map().values().bytesType()
                .noDefault().endRecord(),
            Resolver.Action.Type.ERROR },
        { SchemaBuilder.fixed("HandshakeRequest").namespace("org.apache.avro.ipc").size(16), null,
            Resolver.Action.Type.ERROR },

        { SchemaBuilder.builder("testInt").intType(), SchemaBuilder.builder("testInt").intType(),
            Resolver.Action.Type.DO_NOTHING },

        { SchemaBuilder.enumeration("testEnum").symbols("Value1", "Value2", "value3"),
            SchemaBuilder.enumeration("testEnum").symbols("Value1", "value2", "value5"), Resolver.Action.Type.ENUM },
        { SchemaBuilder.array().items(SchemaBuilder.builder().booleanType()),
            SchemaBuilder.array().items(SchemaBuilder.builder().stringType()), Resolver.Action.Type.CONTAINER },
        { SchemaBuilder.unionOf().fixed("Part1").size(5).and().fixed("Part2").size(3).endUnion(),
            SchemaBuilder.fixed("Part3").size(5), Resolver.Action.Type.WRITER_UNION },
        { SchemaBuilder.fixed("Part1").size(5),
            SchemaBuilder.unionOf().fixed("Part1").size(5).and().fixed("Part2").size(3).endUnion(),
            Resolver.Action.Type.READER_UNION },
        { SchemaBuilder.record("Record").fields().name("Field1").type().intType().noDefault().name("Field2").type()
            .nullType().noDefault().endRecord(),
            SchemaBuilder.record("Record").fields().name("Field1").type().intType().noDefault().name("Field2").type()
                .nullType().noDefault().endRecord(),
            Resolver.Action.Type.RECORD },
        { SchemaBuilder.builder().intType(), SchemaBuilder.builder().longType(), Resolver.Action.Type.PROMOTE },
        { SchemaBuilder.fixed("Fixed").size(10), SchemaBuilder.fixed("Fixed").size(8), Resolver.Action.Type.ERROR },
        { SchemaBuilder.fixed("Fixed").size(10), SchemaBuilder.fixed("Fixed").size(10),
            Resolver.Action.Type.DO_NOTHING },
        { SchemaBuilder.fixed("Fixed2").size(10), SchemaBuilder.fixed("Fixed").size(10), Resolver.Action.Type.ERROR },
        { SchemaBuilder.map().values(SchemaBuilder.builder().intType()),
            SchemaBuilder.map().values(SchemaBuilder.builder().intType()), Resolver.Action.Type.CONTAINER },
        /*
         * { SchemaBuilder.record("invalid").fields().endRecord(),
         * SchemaBuilder.record("invalid").fields().endRecord(),
         * Resolver.Action.Type.SKIP },
         */
    });
  }

  public ResolverTest(Schema writer, Schema reader, Resolver.Action.Type action)
      throws NoSuchFieldException, IllegalAccessException {
    configure(writer, reader, action);
  }

  private void configure(Schema writer, Schema reader, Resolver.Action.Type action)
      throws NoSuchFieldException, IllegalAccessException {
    if (action.equals(Resolver.Action.Type.SKIP)) { // Dato che il test è commentato non verrà mai eseguito
      type = Mockito.mock(Schema.Type.class); // Mantenuto per cronaca
      when(type.getName()).thenReturn("value");
      Field field = Schema.class.getDeclaredField("type");
      Field modifiers = Field.class.getDeclaredField("modifiers");
      field.setAccessible(true);
      modifiers.setAccessible(true);
      modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      field.set(writer, type);
      field.set(reader, type);
    }
    this.writeSchema = writer;
    this.readSchema = reader;
    this.action = action;

  }

  @Test
  public void testResolve() {
    Resolver.Action resultAction = null;
    try {
      resultAction = Resolver.resolve(writeSchema, readSchema);
    } catch (Exception e) {

      Assert.assertEquals(e.getClass(), NullPointerException.class);
      return;
    }
    if (resultAction == null)
      Assert.fail();
    else
      Assert.assertEquals(action, resultAction.type);
  }

}
