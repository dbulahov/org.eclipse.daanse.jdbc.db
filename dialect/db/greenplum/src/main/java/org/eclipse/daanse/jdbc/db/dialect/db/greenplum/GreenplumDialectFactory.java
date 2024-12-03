/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.dialect.db.greenplum;

import java.sql.Connection;
import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractDialectFactory;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.osgi.service.component.annotations.Component;

import aQute.bnd.annotation.spi.ServiceProvider;

@ServiceProvider(value = DialectFactory.class, attribute = { "database.dialect.type:String='POSTGRESQL'",
    "database.product:String='GREENPLUM'" })
@Component(service = DialectFactory.class)
public class GreenplumDialectFactory extends AbstractDialectFactory<GreenplumDialect> {
    private static final String SUPPORTED_PRODUCT_NAME = "GREENPLUM";

    @Override
    public boolean isSupportedProduct(String productName, String productVersion, MetaInfo metaInfo) {
        return SUPPORTED_PRODUCT_NAME.equalsIgnoreCase(productVersion) && isDatabase(SUPPORTED_PRODUCT_NAME, metaInfo);
    }

    @Override
    public Function<MetaInfo, GreenplumDialect> getConstructorFunction() {
        return GreenplumDialect::new;
    }

}
