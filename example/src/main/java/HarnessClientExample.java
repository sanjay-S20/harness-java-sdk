/*
 * Copyright ActionML, LLC under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * ActionML licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.actionml.HarnessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.Optional;

public class HarnessClientExample {
    private static Logger log = LoggerFactory.getLogger(HarnessClientExample.class);

    public static void main(String[] args) {

        String serverHost = null;
        try {
            serverHost = args[0]; // this should be https://... for TLS/SSL when Harness has TLS/SSL enabled
        } catch (Exception e) {
            serverHost = "http://localhost";
        }
        String engineId = null;
        try {
            engineId = args[1];
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        String fileName = null;
        try {
            fileName = args[2];
        } catch (Exception e) {
            e.printStackTrace();
        }
        Integer serverPort = 9090;

        log.info("Args: {}, {}, {}, {}", engineId, fileName, serverHost, serverPort);

        Map<String, String> env = System.getenv();
        Optional<String> optUsername = Optional.ofNullable(env.getOrDefault("HARNESS_CLIENT_USER_ID", null));
        Optional<String> optPassword = Optional.ofNullable(env.getOrDefault("HARNESS_CLIENT_USER_SECRET", null));
        Optional<PasswordAuthentication> optionalCreds = optUsername.flatMap(username -> optPassword.map(password ->
                new PasswordAuthentication(username, password.toCharArray())
        ));

        HarnessClient client = new HarnessClient(engineId, serverHost, serverPort, optionalCreds);

        runGetUserData(client, "u1");
    }


    private static void runGetUserData(HarnessClient client, String userId) {
        client.getEvents(userId).thenApply(x -> { System.out.println(x); return new Object(); });
    }
}
