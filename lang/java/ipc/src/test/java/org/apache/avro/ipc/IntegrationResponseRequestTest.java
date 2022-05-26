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

package org.apache.avro.ipc;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.ipc.generic.GenericRequestor;
import org.apache.avro.ipc.generic.GenericResponder;
import org.apache.avro.Protocol;
import org.apache.avro.util.Utf8;
import org.apache.avro.AvroRuntimeException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class IntegrationResponseRequestTest {

  private static Protocol proto;
  private Transceiver transceiver;
  private SaslSocketServer server;

  private GenericRequestor req;

  private String method;

  private Object result;

  private Object[][] params;

  private static class EasyResponder extends GenericResponder {
    public EasyResponder(Protocol protocol) {
      super(protocol);
    }

    @Override
    public Object respond(Protocol.Message message, Object request) throws Exception {
      GenericRecord received = (GenericRecord) request;

      if (message.getName().equals("hello"))
        return new Utf8("goodbye");

      if (message.getName().equals("echo"))
        return received.get("record");

      if (message.getName().equals("error")) {
        GenericRecord error = new GenericData.Record(proto.getType("TestError"));
        error.put("message", new Utf8("an error"));
        throw new AvroRemoteException(error);
      }

      throw new AvroRuntimeException("Invalid message :" + message.getName());
    }
  }

  @Parameterized.Parameters
  public static Collection<Object[]> testParameters() throws IOException {
    Protocol msg = Protocol.parse(new File("../../../share/test/schemas/mail.avpr"));
    GenericData.Record message = new GenericData.Record(msg.getType("Message"));
    message.put("to", "test");
    message.put("from", "davide");
    message.put("body", "hello!");
    return Arrays.asList(new Object[][] { { "simple", "ack", new Object[][] {}, null },
        { "simple", "hello", new Object[][] { { "greeting", "bob" } }, new Utf8("goodbye") },
        { "mail", "fireandforget", new Object[][] { { "message", message } }, null },
        { "mail", "send", new Object[][] { { "message", message } }, "sent" }, });
  }

  public IntegrationResponseRequestTest(String protocol, String method, Object[][] params, Object result)
      throws IOException {
    configure(protocol, method, params, result);
  }

  private void configure(String protocol, String method, Object[][] params, Object result) throws IOException {

    File f = new File("../../../share/test/schemas/" + protocol + ".avpr");
    this.proto = Protocol.parse(f);
    this.method = method;
    this.params = params;
    this.result = result;
    server = new SaslSocketServer(new EasyResponder(proto), new InetSocketAddress(420));

  }

  @Before
  public void start() throws IOException {
    server.start();
    transceiver = new SaslSocketTransceiver(new InetSocketAddress(server.getPort()));
    this.req = new GenericRequestor(proto, transceiver);

  }

  @Test
  public void testResponseRequest() throws Exception {
    GenericData.Record record = new GenericData.Record(proto.getMessages().get(method).getRequest());

    if (params.length > 0)
      for (int i = 0; i < params.length; i++) {
        record.put(params[i][0].toString(), params[i][1]);
      }
    Assert.assertEquals(this.result, req.request(this.method, record));
  }

  @After
  public void stop() throws IOException {
    server.close();
    transceiver.close();
  }

}
