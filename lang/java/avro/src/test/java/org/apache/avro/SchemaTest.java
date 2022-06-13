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
import java.util.logging.Level;
import java.util.logging.Logger;

@RunWith(value = Parameterized.class)
public class SchemaTest {

  private Schema writer;
  private Schema reader;

  @Parameterized.Parameters
  public static Collection<Object[]> testParameters() {
    return Arrays.asList(new Object[][] {
        { SchemaBuilder.builder().record("Record1").aliases("Alias").fields().name("Field1").aliases("Alias1").type()
            .intType().noDefault().name("Field2").prop("Custom", "value").aliases("Alias2").type().stringType()
            .noDefault().endRecord(),
            SchemaBuilder.builder().record("Record").aliases("Record1").fields().name("Field3").aliases("Field1").type()
                .intType().noDefault().name("Field4").aliases("Field2").prop("Custom", "value").type().stringType()
                .noDefault().endRecord() },
        { null,
            SchemaBuilder.builder().record("Record1").fields().name("Field1").type().intType().noDefault()
                .name("field2").type().stringType().noDefault().endRecord() },
        { SchemaBuilder.builder().record("Record1").fields().name("Field1").type().intType().noDefault().name("field2")
            .type().stringType().noDefault().endRecord(), null },
        { SchemaBuilder.builder().record("Record1").fields().name("Field1").type().intType().noDefault().name("field2")
            .type().stringType().noDefault().endRecord(),
            SchemaBuilder.builder().record("Record1").fields().name("Field1").type().intType().noDefault()
                .name("field2").type().stringType().noDefault().endRecord() },
        { SchemaBuilder.builder().enumeration("Enum1").symbols("Symbol1", "Symbol2"),
            SchemaBuilder.builder().enumeration("Enum2").aliases("Enum1").symbols("Symbol3", "Symbol4") },
        { SchemaBuilder.fixed("Fixed1").aliases("Fix").prop("Prop1", "val1").prop("Prop2", "val2").size(10),
            SchemaBuilder.fixed("Fixed2").aliases("Fixed1").prop("Prop2", "val2").aliases("Prop1").prop("Prop4", "val4")
                .aliases("Prop2").size(10) },
        { SchemaBuilder.array()
            .items(SchemaBuilder.record("Item1").fields().name("Field1").type().intType().noDefault().endRecord()),
            SchemaBuilder.array()
                .items(SchemaBuilder.record("Item2").aliases("Item1").fields().name("Field2").aliases("Field1").type()
                    .intType().noDefault().endRecord()) },
        { SchemaBuilder.array()
            .items(SchemaBuilder.record("Item1").fields().name("Field1").type().intType().noDefault().endRecord()),
            SchemaBuilder.array()
                .items(SchemaBuilder.record("Item3").fields().name("Field2").aliases("Field3").type().intType()
                    .noDefault().endRecord()) },
        { SchemaBuilder.map()
            .values(SchemaBuilder.record("Item1").fields().name("Field1").type().intType().noDefault().endRecord()),
            SchemaBuilder.map()
                .values(SchemaBuilder.record("Item2").aliases("Item1").fields().name("Field2").aliases("Field1").type()
                    .intType().noDefault().endRecord()) },
        { SchemaBuilder.map()
            .values(SchemaBuilder.record("Item1").fields().name("Field1").type().intType().noDefault().endRecord()),
            SchemaBuilder.map()
                .values(SchemaBuilder.record("Item3").fields().name("Field2").aliases("Field3").type().intType()
                    .noDefault().endRecord()) },
        { SchemaBuilder.unionOf().record("Part1").fields().name("Field1").type().intType().noDefault().endRecord().and()
            .record("Part2").fields().name("Field2").type().booleanType().noDefault().endRecord().endUnion(),
            SchemaBuilder.unionOf().record("Part3").aliases("Part1").fields().name("Field3").aliases("Field1").type()
                .intType().noDefault().endRecord().and().record("Part4").aliases("Part2").fields().name("Field4")
                .aliases("Field2").type().booleanType().noDefault().endRecord().endUnion() },
        { SchemaBuilder.builder().record("Record").fields().name("Field1").type().intType().noDefault().name("Field2")
            .type().stringType().noDefault().endRecord(),
            SchemaBuilder.builder().record("Record2").fields().name("Field3").type().intType().noDefault()
                .name("Field4").type().stringType().noDefault().endRecord() },
        { SchemaBuilder.builder().record("Record").fields().name("Field1").type().intType().noDefault().name("Field2")
            .type().stringType().noDefault().endRecord(),
            SchemaBuilder.builder().record("Record1").aliases("AnotherAlias").fields().name("Field3").type().intType()
                .noDefault().name("Field4").type().stringType().noDefault().endRecord() },
        { SchemaBuilder.builder().enumeration("Enum1").symbols("Symbol1"),
            SchemaBuilder.builder().enumeration("Enum2").aliases("Enum3").symbols("Symbol3") },
        { SchemaBuilder.fixed("Fix").prop("Prop1", "val1").size(10),
            SchemaBuilder.fixed("Fixed2").prop("Prop2", "val4").aliases("Prop2").size(10) },
        { SchemaBuilder.builder().record("Record1").fields().name("Field1").type().intType().noDefault().name("field2")
            .type().stringType().noDefault().endRecord(),
            SchemaBuilder.builder().record("Record").aliases("Record1").fields().name("Field1").type().intType()
                .noDefault().name("field2").type().stringType().noDefault().endRecord() }, });
  }

  public SchemaTest(Schema writer, Schema reader) {
    configure(writer, reader);
  }

  private void configure(Schema writer, Schema reader) {

    try {
      if (writer.getType().equals(Schema.Type.FIXED))
        reader.addAlias("Fixed1");
    } catch (Exception e) {
      //
    }
    this.writer = writer;
    this.reader = reader;

  }

  @Test
  public void testApplyAliases() {
    Schema result = null;
    try {
      result = Schema.applyAliases(writer, reader);
      if (result == null)
        Assert.fail();
      switch (result.getType().ordinal()) {
      case 0: // Record
        Logger.getGlobal().log(Level.INFO, result.getFields().toString() + "reader" + reader.getFields().toString());
        if (!reader.getAliases().contains(writer.getName())) {

          Assert.assertEquals(writer, result);
          Assert.assertEquals(writer.getFields(), result.getFields());
        } else {

          Assert.assertEquals(reader, result);
          Assert.assertEquals(reader.getFields(), result.getFields());

        }
        break;
      case 1: // ENUM
        if (reader.getAliases().contains(writer.getName()))
          Assert.assertEquals(reader.getName(), result.getName());
        else
          Assert.assertEquals(writer.getName(), result.getName());
        break;
      case 5: // FIXED

        Assert.assertEquals(writer.getObjectProps(), result.getObjectProps());
        if (reader.getAliases().contains(writer.getName()))
          Assert.assertEquals(reader.getName(), result.getName());
        break;
      case 2:
        if (reader.getElementType().getAliases().contains(writer.getElementType().getName()))
          Assert.assertEquals(reader.getElementType(), result.getElementType());
        else
          Assert.assertEquals(writer.getElementType(), result.getElementType());
        break;
      case 3:
        if (reader.getValueType().getAliases().contains(writer.getValueType().getName()))
          Assert.assertEquals(reader.getValueType(), result.getValueType());
        else
          Assert.assertEquals(writer.getValueType(), result.getValueType());
        break;
      case 4:
        Assert.assertEquals(reader.getTypes(), result.getTypes());
        break;
      }
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), NullPointerException.class);
    }

  }
}
