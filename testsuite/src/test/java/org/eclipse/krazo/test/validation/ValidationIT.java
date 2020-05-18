/*
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018, 2019 Eclipse Krazo committers and contributors
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.krazo.test.validation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.eclipse.krazo.test.util.WebArchiveBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ValidationIT {

    private static final String WEB_INF_SRC = "src/main/resources/validation/";

    @ArquillianResource
    private URL baseURL;

    private WebClient webClient;

    @Before
    public void setUp() throws Exception {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setRedirectEnabled(true);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Deployment(testable = false, name = "validation")
    public static Archive createDeployment() {
        return new WebArchiveBuilder().addPackage("org.eclipse.krazo.test.validation")
            .addView(Paths.get(WEB_INF_SRC).resolve("views/error.jsp").toFile(), "error.jsp")
            .addView(Paths.get(WEB_INF_SRC).resolve("views/success.jsp").toFile(), "success.jsp")
            .addView(Paths.get(WEB_INF_SRC).resolve("views/form.jsp").toFile(), "form.jsp")
            .addBeansXml()
            .build();
    }

    /**
     * Test form submission to the basic controller endpoint using a good
     * color value. We should get a success response.
     * <p>
     * This test works as expected.
     *
     * @throws IOException
     */
    @Test
    public void testColorFormBasicGoodColor() throws IOException {

        // GET the color form, populate a color and then submit it
        final HtmlPage formPage = webClient.getPage(baseURL + "resources/color/basic");

        final HtmlForm form = formPage.getFormByName("testForm");
        final HtmlTextInput colorInput = form.getInputByName("color");
        final HtmlSubmitInput submitButton = form.getInputByName("submit");

        colorInput.type("orange"); // 'orange' is a good color

        final HtmlPage resultPage = submitButton.click();
        final Iterator<HtmlElement> h1Iter = resultPage.getDocumentElement().getElementsByTagName("h1").iterator();
        assertEquals("Success", h1Iter.next().asText());
    }

    /**
     * Test form submission to the EJB-backed controller endpoint using a good
     * color value. We should get a success response.
     * <p>
     * This test works as expected.
     *
     * @throws IOException
     */
    @Test
    public void testColorFormEjbGoodColor() throws IOException {

        // GET the color form, populate a color and then submit it
        final HtmlPage formPage = webClient.getPage(baseURL + "resources/color/ejb");

        final HtmlForm form = formPage.getFormByName("testForm");
        final HtmlTextInput colorInput = form.getInputByName("color");
        final HtmlSubmitInput submitButton = form.getInputByName("submit");

        colorInput.type("yellow"); // 'yellow' is a good color

        final HtmlPage resultPage = submitButton.click();
        final Iterator<HtmlElement> h1Iter = resultPage.getDocumentElement().getElementsByTagName("h1").iterator();
        assertEquals("Success", h1Iter.next().asText());
    }

    /**
     * Test form submission to the basic controller endpoint using a
     * blank/empty color value. We should get an error page that will contain
     * a message about the not-blank color validation constraint failure.
     * <p>
     * This test works as expected.
     *
     * @throws IOException
     */
    @Test
    public void testColorFormBasicBlankColor() throws IOException {

        // GET the color form and then submit it without populating any color value
        final HtmlPage formPage = webClient.getPage(baseURL + "resources/color/basic");

        final HtmlForm form = formPage.getFormByName("testForm");
        final HtmlSubmitInput submitButton = form.getInputByName("submit");

        final HtmlPage resultPage = submitButton.click();
        final Iterator<HtmlElement> h1Iter = resultPage.getDocumentElement().getElementsByTagName("h1").iterator();
        assertEquals("Error", h1Iter.next().asText());
        final Iterator<HtmlElement> h3Iter = resultPage.getDocumentElement().getElementsByTagName("h3").iterator();
        assertEquals("color must not be blank", h3Iter.next().asText());
    }

    /**
     * Test form submission to the EJB-backed controller endpoint using a
     * blank/empty color value. We should get an error page that will contain
     * a message about the not-blank color validation constraint failure.
     * <p>
     * This test works as expected since the controller handles the validation
     * failure in the binding result and never invokes the backing EJB.
     *
     * @throws IOException
     */
    @Test
    public void testColorFormEjbBlankColor() throws IOException {

        // GET the color form and then submit it without populating any color value
        final HtmlPage formPage = webClient.getPage(baseURL + "resources/color/ejb");

        final HtmlForm form = formPage.getFormByName("testForm");
        final HtmlSubmitInput submitButton = form.getInputByName("submit");

        final HtmlPage resultPage = submitButton.click();
        final Iterator<HtmlElement> h1Iter = resultPage.getDocumentElement().getElementsByTagName("h1").iterator();
        assertEquals("Error", h1Iter.next().asText());
        final Iterator<HtmlElement> h3Iter = resultPage.getDocumentElement().getElementsByTagName("h3").iterator();
        assertEquals("color must not be blank", h3Iter.next().asText());
    }

    /**
     * Test form submission to the basic controller endpoint using a
     * 'bad' color value that won't pass @GoodColor validation successfully.
     * <p>
     * We should get an error page that will contain a message about the
     * color not being good, but instead we get a success page. This shows
     * that the MVC binding result does not handle the @GoodColor constraint.
     *
     * @throws IOException
     */
    @Test
    public void testColorFormBasicBadColor() throws IOException {

        // GET the color form and then submit it with a color value that won't pass the type @GoodColor constraint
        final HtmlPage formPage = webClient.getPage(baseURL + "resources/color/basic");

        final HtmlForm form = formPage.getFormByName("testForm");
        final HtmlTextInput colorInput = form.getInputByName("color");
        final HtmlSubmitInput submitButton = form.getInputByName("submit");

        colorInput.type("blue"); // 'blue' is a not good color

        final HtmlPage resultPage = submitButton.click();
        final Iterator<HtmlElement> h1Iter = resultPage.getDocumentElement().getElementsByTagName("h1").iterator();
        assertEquals("Error", h1Iter.next().asText());
        final Iterator<HtmlElement> h3Iter = resultPage.getDocumentElement().getElementsByTagName("h3").iterator();
        assertEquals("That is not a good color", h3Iter.next().asText());
    }

    /**
     * Test form submission to the EJB-backed controller endpoint using a
     * 'bad' color value that won't pass @GoodColor validation successfully.
     * <p>
     * We should get an error page that will contain a message about the
     * color not being good, but instead the backend server will throw an
     * EJBException since the MVC binding result won't indicate any failures,
     * and the ColorFormModel param passed to the EJB really isn't valid.
     *
     * @throws IOException
     */
    @Test
    public void testColorFormEjbBadColor() throws IOException {

        // GET the color form and then submit it with a color value that won't pass the type @GoodColor constraint
        final HtmlPage formPage = webClient.getPage(baseURL + "resources/color/ejb");

        final HtmlForm form = formPage.getFormByName("testForm");
        final HtmlTextInput colorInput = form.getInputByName("color");
        final HtmlSubmitInput submitButton = form.getInputByName("submit");

        colorInput.type("blue"); // 'blue' is a not good color

        final HtmlPage resultPage = submitButton.click();
        final Iterator<HtmlElement> h1Iter = resultPage.getDocumentElement().getElementsByTagName("h1").iterator();
        assertEquals("Error", h1Iter.next().asText());
        final Iterator<HtmlElement> h3Iter = resultPage.getDocumentElement().getElementsByTagName("h3").iterator();
        assertEquals("That is not a good color", h3Iter.next().asText());
    }
}
