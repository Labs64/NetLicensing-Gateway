/* Licensed under the Apache License, Version 2.0 (the "License");
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
package com.labs64.netlicensing.gateway.controller.restful;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.gateway.controller.restful.MyCommerceController;
import com.labs64.netlicensing.service.LicenseService;

/**
 * Integration tests for {@link LicenseService}.
 */
public class MyCommerceControllerTest extends BaseControllerTest {

    private static final String LICENSEE_CUSTOM_PROPERTY = "CustomProperty";
    private static final String LICENSEE_DELETING_PROPERTY = "toBeDeleted";

    final String productNumber = "P001-TEST";
    final String licenseeNumber = "L001-TEST";

    // *** NLIC Tests ***

    private static Context context;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        context = createContext();
    }

    // @Test
    // public void testCreateEmpty() {
    // assertTrue("1", true);
    // }

    @Override
    protected Class<?> getResourceClass() {
        return MyCommerceController.class;
    }
}
