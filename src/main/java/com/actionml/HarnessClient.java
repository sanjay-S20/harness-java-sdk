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

package com.actionml;

import akka.http.javadsl.model.Uri;
import akka.japi.Pair;
import akka.stream.javadsl.Source;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author The ActionML Team (<a href="http://actionml.com">http://actionml.com</a>)
 *         26.02.17 18:02
 */
public class HarnessClient extends RestClient {

    public HarnessClient(String engineId, String host, Integer port, Optional<PasswordAuthentication> optionalCreds) {
        super(host, port, Uri.create("/engines").addPathSegment(engineId).addPathSegment("entities"), optionalCreds, Optional.empty());
    }

    public CompletionStage<Pair<Integer, List<String>>> getEvents(String userId) {
        return withAuth().thenCompose(optionalToken ->
                Source.single(this.uri.addPathSegment(userId))
                        .map(this::createGet)
                        .zipWithIndex()
                        .map(pair -> pair.copy(pair.first(), (Long) pair.second()))
                        .via(this.poolClientFlow)
                        .mapAsync(1, this::extractResponse)
                        .mapAsync(1, this::extractResponses)
                        .runFold(Pair.create(0, new ArrayList<>()), (acc, pair) -> {
                            Integer code = pair.second().first();
                            List<String> events = acc.second();
                            events.add(pair.second().second());
                            return Pair.create(code, events);
                        }, this.materializer)
        );
    }

    public CompletionStage<Integer> deleteEvents(String userId) {
        return withAuth().thenCompose(optionalToken ->
                Source.single(this.uri.addPathSegment(userId))
                        .map(this::createDelete)
                        .zipWithIndex()
                        .map(pair -> pair.copy(pair.first(), (Long) pair.second()))
                        .via(this.poolClientFlow)
                        .mapAsync(1, this::extractResponse)
                        .runFold(0, (a, i) -> i.second().status().intValue(), this.materializer)
        );
    }


}
