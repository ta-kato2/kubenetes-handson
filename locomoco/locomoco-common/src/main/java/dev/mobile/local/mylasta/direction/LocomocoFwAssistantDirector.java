/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package dev.mobile.local.mylasta.direction;

import java.util.List;

import javax.annotation.Resource;

import dev.mobile.local.mylasta.direction.sponsor.LocomocoActionAdjustmentProvider;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoCookieResourceProvider;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoCurtainBeforeHook;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoJsonResourceProvider;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoMailDeliveryDepartmentCreator;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoSecurityResourceProvider;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoTimeResourceProvider;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoUserLocaleProcessProvider;
import dev.mobile.local.mylasta.direction.sponsor.LocomocoUserTimeZoneProcessProvider;
import org.lastaflute.core.direction.CachedFwAssistantDirector;
import org.lastaflute.core.direction.CurtainBeforeHook;
import org.lastaflute.core.direction.FwAssistDirection;
import org.lastaflute.core.direction.FwCoreDirection;
import org.lastaflute.core.json.JsonResourceProvider;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.core.security.OneWayCryptographer;
import org.lastaflute.core.security.SecurityResourceProvider;
import org.lastaflute.core.time.TimeResourceProvider;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.direction.FwDbDirection;
import org.lastaflute.web.api.ApiFailureHook;
import org.lastaflute.web.direction.FwWebDirection;
import org.lastaflute.web.path.ActionAdjustmentProvider;
import org.lastaflute.web.servlet.cookie.CookieResourceProvider;
import org.lastaflute.web.servlet.request.UserLocaleProcessProvider;
import org.lastaflute.web.servlet.request.UserTimeZoneProcessProvider;

/**
 * @author jflute
 */
public abstract class LocomocoFwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private LocomocoConfig config;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    @Override
    protected void prepareAssistDirection(FwAssistDirection direction) {
        direction.directConfig(nameList -> setupAppConfig(nameList), "locomoco_config.properties", "locomoco_env.properties");
    }

    protected abstract void setupAppConfig(List<String> nameList);

    // ===================================================================================
    //                                                                               Core
    //                                                                              ======
    @Override
    protected void prepareCoreDirection(FwCoreDirection direction) {
        // this configuration is on locomoco_env.properties because this is true only when development
        direction.directDevelopmentHere(config.isDevelopmentHere());

        // titles of the application for logging are from configurations
        direction.directLoggingTitle(config.getDomainTitle(), config.getEnvironmentTitle());

        // this configuration is on sea_env.properties because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(config.isFrameworkDebug()); // basically false

        // you can add your own process when your application's curtain before
        direction.directCurtainBefore(createCurtainBeforeHook());

        direction.directSecurity(createSecurityResourceProvider());
        direction.directTime(createTimeResourceProvider());
        direction.directJson(createJsonResourceProvider());
        direction.directMail(createMailDeliveryDepartmentCreator().create());
    }

    protected CurtainBeforeHook createCurtainBeforeHook() {
        return new LocomocoCurtainBeforeHook();
    }

    protected SecurityResourceProvider createSecurityResourceProvider() { // #change_it_first
        final InvertibleCryptographer inver = InvertibleCryptographer.createAesCipher("locomoco:dockside");
        final OneWayCryptographer oneWay = OneWayCryptographer.createSha256Cryptographer();
        return new LocomocoSecurityResourceProvider(inver, oneWay);
    }

    protected TimeResourceProvider createTimeResourceProvider() {
        return new LocomocoTimeResourceProvider(config);
    }

    protected JsonResourceProvider createJsonResourceProvider() {
        return new LocomocoJsonResourceProvider();
    }

    protected LocomocoMailDeliveryDepartmentCreator createMailDeliveryDepartmentCreator() {
        return new LocomocoMailDeliveryDepartmentCreator(config);
    }

    // ===================================================================================
    //                                                                                 DB
    //                                                                                ====
    @Override
    protected void prepareDbDirection(FwDbDirection direction) {
        direction.directClassification(createListedClassificationProvider());
    }

    protected abstract ListedClassificationProvider createListedClassificationProvider();

    // ===================================================================================
    //                                                                                Web
    //                                                                               =====
    @Override
    protected void prepareWebDirection(FwWebDirection direction) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider());
        direction.directCookie(createCookieResourceProvider());
        direction.directAdjustment(createActionAdjustmentProvider());
        direction.directMessage(nameList -> setupAppMessage(nameList), "locomoco_message", "locomoco_label");
        direction.directApiCall(createApiFailureHook());
    }

    protected UserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new LocomocoUserLocaleProcessProvider();
    }

    protected UserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new LocomocoUserTimeZoneProcessProvider();
    }

    protected CookieResourceProvider createCookieResourceProvider() { // #change_it_first
        final InvertibleCryptographer cr = InvertibleCryptographer.createAesCipher("dockside:locomoco");
        return new LocomocoCookieResourceProvider(config, cr);
    }

    protected ActionAdjustmentProvider createActionAdjustmentProvider() {
        return new LocomocoActionAdjustmentProvider();
    }

    protected abstract void setupAppMessage(List<String> nameList);

    protected abstract ApiFailureHook createApiFailureHook();
}
