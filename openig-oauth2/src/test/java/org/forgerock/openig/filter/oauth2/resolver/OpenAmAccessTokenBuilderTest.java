/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2016 ForgeRock AS.
 */

package org.forgerock.openig.filter.oauth2.resolver;

import static org.assertj.core.api.Assertions.*;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openig.el.Bindings.bindings;
import static org.mockito.Mockito.*;

import org.forgerock.authz.modules.oauth2.AccessTokenException;
import org.forgerock.json.JsonValue;
import org.forgerock.openig.el.Bindings;
import org.forgerock.openig.el.Expression;
import org.forgerock.util.time.TimeService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class OpenAmAccessTokenBuilderTest {

    public static final String TOKEN = "45b4c835-2617-4c39-9dc8-708ae493975f5f";

    @Mock
    private TimeService time;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(time.now()).thenReturn(42L);
    }

    @Test
    public void shouldProduceAccessToken() throws Exception {
        OpenAmAccessToken.Builder builder = new OpenAmAccessToken.Builder(time);

        JsonValue info = json(object(
                field("expires_in", 10),
                field("access_token", TOKEN),
                field("scope", array("email", "address"))));

        OpenAmAccessToken token = builder.build(info);

        assertThat(token.getToken()).isEqualTo(TOKEN);
        assertThat(token.getExpiresAt()).isEqualTo(10000L + 42L);
        assertThat(token.getScopes()).containsOnly("email", "address");
        assertThat(token.getInfo().get("expires_in")).isEqualTo(10);
        Bindings bindings = bindings("token", token);
        assertThat(Expression.valueOf("${token.info.scope[0]}", String.class).eval(bindings)).isEqualTo("email");
        assertThat(Expression.valueOf("${token.info.scope[1]}", String.class).eval(bindings)).isEqualTo("address");
        assertThat(Expression.valueOf("${token.info.access_token}", String.class).eval(bindings)).isEqualTo(TOKEN);
        assertThat(token.asJsonValue()).isSameAs(info);
    }

    @DataProvider
    public Object[][] invalidJsonStructures() {
        // @Checkstyle:off
        return new Object[][] {
                {missingExpiresIn()},
                {missingAccessToken()},
                {missingScope()},
                {wrongExpiresInType()},
                {wrongScopeType()}
        };
        // @Checkstyle:on
    }

    @Test(dataProvider = "invalidJsonStructures",
          expectedExceptions = AccessTokenException.class)
    public void shouldFailBecauseOfMissingAttribute(JsonValue tokenInfo) throws Exception {
        new OpenAmAccessToken.Builder(time).build(tokenInfo);
    }

    private JsonValue missingExpiresIn() {
        return json(object(
                field("access_token", TOKEN),
                field("scope", array("email", "address"))));
    }

    private JsonValue missingAccessToken() {
        return json(object(
                field("expires_in", 10),
                field("scope", array("email", "address"))));
    }

    private JsonValue missingScope() {
        return json(object(
                field("expires_in", 10),
                field("access_token", TOKEN)));
    }

    private JsonValue wrongExpiresInType() {
        return json(object(
                field("expires_in", "10"),
                field("access_token", TOKEN),
                field("scope", array("email", "address"))));
    }

    private JsonValue wrongScopeType() {
        return json(object(
                field("expires_in", 10),
                field("access_token", TOKEN),
                field("scope", "email address")));
    }
}
