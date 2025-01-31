//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

import org.eclipse.jetty.ee9.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.ee9.plus.webapp.PlusConfiguration;

module org.eclipse.jetty.ee9.plus
{
    requires org.eclipse.jetty.util;
    requires org.slf4j;

    requires transitive org.eclipse.jetty.ee9.webapp;
    requires transitive org.eclipse.jetty.plus;

    // Only required if using Transaction.
    requires static transitive jakarta.transaction;
    requires org.eclipse.jetty.jndi;

    exports org.eclipse.jetty.ee9.plus.jndi;
    exports org.eclipse.jetty.ee9.plus.webapp;

    provides org.eclipse.jetty.ee9.webapp.Configuration with
        EnvConfiguration,
        PlusConfiguration;
}
