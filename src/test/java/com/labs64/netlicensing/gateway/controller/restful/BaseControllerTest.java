/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.labs64.netlicensing.gateway.controller.restful;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import com.labs64.netlicensing.domain.Constants;
import com.labs64.netlicensing.domain.vo.SecurityMode;
import com.labs64.netlicensing.schema.SchemaFunction;
import com.labs64.netlicensing.schema.context.InfoEnum;
import com.labs64.netlicensing.schema.context.Item;
import com.labs64.netlicensing.schema.context.Netlicensing;
import com.labs64.netlicensing.schema.context.ObjectFactory;
import com.labs64.netlicensing.schema.context.Property;
import com.labs64.netlicensing.util.JAXBUtils;

/**
 * Base class for integration tests for NetLicensing services.
 */
abstract class BaseControllerTest extends JerseyTest {

    static final String REST_API_PATH = "/core/v2/rest";
    static final String BASE_URL_UNITTEST = "http://localhost:9998";
    static final String BASE_URL_PROD = "https://go.netlicensing.io";

    static final String BASE_URL = BASE_URL_UNITTEST + REST_API_PATH;

    static final String USER = "demo";
    static final String PASS = "demo";

    static final String TEST_CASE_BASE = "mock/";

    static com.labs64.netlicensing.domain.vo.Context createContext() {
        return new com.labs64.netlicensing.domain.vo.Context().setBaseUrl(BASE_URL)
                .setSecurityMode(SecurityMode.BASIC_AUTHENTICATION).setUsername(USER).setPassword(PASS);
    }

    @Override
    protected final Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(getResourceClass());
    }

    /**
     * @return NLIC mock resource class
     */
    protected abstract Class<?> getResourceClass();

    // *** Abstract NLIC service test mock resource ***

    public static abstract class AbstractNLICServiceResource {

        /** ID of the service, i.e. "product", "licensee", etc */
        private final String serviceId;

        protected final ObjectFactory objectFactory = new ObjectFactory();

        /**
         * @param serviceId
         *            service identifier
         */
        public AbstractNLICServiceResource(final String serviceId) {
            this.serviceId = serviceId;
        }

        /**
         * Mock for "create entity" service.
         *
         * @param formParams
         *            POST request body parameters
         * @return response with XML representation of the created entity
         */
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response create(final MultivaluedMap<String, String> formParams) {
            return create(formParams, new HashMap<String, String>());
        }

        /**
         * Mock for "get entity" service.
         *
         * @return response with XML representation of the entity
         */
        @GET
        @Path("{number}")
        public Response get(@PathParam("number") final String number) {
            final String xmlResourcePath = String.format("%snetlicensing-%s-get.xml", TEST_CASE_BASE,
                    serviceId.toLowerCase());
            final Netlicensing netlicensing = JAXBUtils.readObject(xmlResourcePath, Netlicensing.class);
            return Response.ok(netlicensing).build();
        }

        /**
         * Mock for "list entities" service.
         *
         * @return response with XML representation of the entities page
         */
        @GET
        public Response list() {
            final String xmlResourcePath = String.format("%snetlicensing-%s-list.xml", TEST_CASE_BASE,
                    serviceId.toLowerCase());
            final Netlicensing netlicensing = JAXBUtils.readObject(xmlResourcePath, Netlicensing.class);
            return Response.ok(netlicensing).build();
        }

        /**
         * Mock for "update entity" service.
         *
         * @param formParams
         *            POST request body parameters
         * @return response with XML representation of the updated entity
         */
        @POST
        @Path("{number}")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response update(@PathParam("number") final String number,
                final MultivaluedMap<String, String> formParams) {
            final String resourcePath = String.format("%snetlicensing-%s-update.xml", TEST_CASE_BASE,
                    serviceId.toLowerCase());
            final Netlicensing netlicensing = JAXBUtils.readObject(resourcePath, Netlicensing.class);

            final List<Property> properties = netlicensing.getItems().getItem().get(0).getProperty();
            for (final String paramKey : formParams.keySet()) {
                final Property property = SchemaFunction.propertyByName(properties, paramKey);
                final String paramValue = formParams.getFirst(paramKey);
                if (paramValue != null && paramValue.trim().equals("")) {
                    properties.remove(property);
                } else {
                    if (!properties.contains(property)) {
                        properties.add(property);
                    }
                    property.setValue(paramValue);
                }
            }

            return Response.ok(netlicensing).build();
        }

        /**
         * Mock for "delete entity" service.
         *
         * @param number
         *            entity number
         * @param uriInfo
         *            context URI info
         * @return response with "No Content" status
         */
        @DELETE
        @Path("{number}")
        public Response delete(@PathParam("number") final String number, @Context final UriInfo uriInfo) {
            return delete(number, "EXPECTED", uriInfo.getQueryParameters());
        }

        /**
         * Defines common functionality for a "create entity" service.
         *
         * @param formParams
         *            POST request body parameters
         * @param defaultPropertyValues
         *            default values for the entity properties
         * @return response with XML representation of the created entity
         */
        protected Response create(final MultivaluedMap<String, String> formParams,
                final Map<String, String> defaultPropertyValues) {
            final Netlicensing netlicensing = objectFactory.createNetlicensing();
            netlicensing.setItems(objectFactory.createNetlicensingItems());

            final Item item = objectFactory.createItem();
            item.setType(WordUtils.capitalize(serviceId));
            netlicensing.getItems().getItem().add(item);

            final Map<String, String> propertyValues = new HashMap<String, String>(defaultPropertyValues);
            for (final String paramKey : formParams.keySet()) {
                propertyValues.put(paramKey, formParams.getFirst(paramKey));
            }

            final List<Property> properties = netlicensing.getItems().getItem().get(0).getProperty();
            for (final String propertyName : propertyValues.keySet()) {
                final Property property = SchemaFunction.propertyByName(properties, propertyName);
                if (!properties.contains(property)) {
                    properties.add(property);
                }
                property.setValue(propertyValues.get(propertyName));
            }

            return Response.ok(netlicensing).build();
        }

        /**
         * Defines common functionality for a "delete entity" service.
         *
         * @param number
         *            entity number
         * @param expectedNumber
         *            expected entity number
         * @param queryParams
         *            query parameters
         * @return response with "No Content" status
         */
        protected Response delete(final String number, final String expectedNumber,
                final MultivaluedMap<String, String> queryParams) {
            if (!expectedNumber.equals(number)) {
                final String entityStr = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(serviceId), ' ')
                        .toLowerCase();
                return errorResponse("NotFoundException", String.format("requested %s does not exist", entityStr));
            }

            // for testing purposes parameter "forceCascade" for "existing" entities should always be true if not absent
            final boolean hasForceCascade = queryParams != null && queryParams.containsKey(Constants.CASCADE);
            if (hasForceCascade && !Boolean.valueOf(queryParams.getFirst(Constants.CASCADE))) {
                return unexpectedValueErrorResponse(Constants.CASCADE);
            }

            return Response.status(Response.Status.NO_CONTENT).build();
        }

        /**
         * Generates error response for the service mock
         *
         * @param errorIdsAndMessages
         *            array where every string with even index is exception ID and every string with odd index is
         *            corresponding error message
         * @return response object
         */
        protected final Response errorResponse(final String... errorIdsAndMessages) {
            if (errorIdsAndMessages.length % 2 != 0) {
                throw new IllegalArgumentException("Some exception ID doesn't have corresponding error message");
            }

            final Netlicensing netlicensing = objectFactory.createNetlicensing();
            for (int i = 0; i < errorIdsAndMessages.length; i += 2) {
                final String exceptionId = errorIdsAndMessages[i];
                final String errorMessage = errorIdsAndMessages[i + 1];
                SchemaFunction.addInfo(netlicensing, exceptionId, InfoEnum.ERROR, errorMessage);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(netlicensing).build();
        }

        /**
         * Generates UnexpectedValueException response for the service mock
         *
         * @param parameterName
         *            parameter name
         * @return response object
         */
        protected final Response unexpectedValueErrorResponse(final String parameterName) {
            return errorResponse("UnexpectedValueException",
                    String.format("Unexpected value of parameter '%s'", parameterName));
        }

        /**
         * @param formParams
         *            form params map
         * @param paramKey
         *            parameter key
         */
        protected void roundParamValueToTwoDecimalPlaces(final MultivaluedMap<String, String> formParams,
                final String paramKey) {
            if (formParams.containsKey(paramKey)) {
                final String priceStr = formParams.getFirst(paramKey);
                final BigDecimal roundedPrice = new BigDecimal(priceStr).setScale(2, BigDecimal.ROUND_HALF_UP);
                formParams.putSingle(paramKey, roundedPrice.toString());
            }
        }

    }

}
