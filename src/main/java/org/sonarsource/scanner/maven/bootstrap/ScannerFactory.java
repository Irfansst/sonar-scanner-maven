/*
 * SonarQube Scanner for Maven
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.scanner.maven.bootstrap;

import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.RuntimeInformation;
import org.sonarsource.scanner.api.EmbeddedScanner;
import org.sonarsource.scanner.api.LogOutput;

public class ScannerFactory {

  private final LogOutput logOutput;
  private final RuntimeInformation runtimeInformation;
  private final MavenSession session;
  private final boolean debugEnabled;
  private PropertyDecryptor propertyDecryptor;

  public ScannerFactory(LogOutput logOutput, boolean debugEnabled, RuntimeInformation runtimeInformation, MavenSession session, PropertyDecryptor propertyDecryptor) {
    this.logOutput = logOutput;
    this.runtimeInformation = runtimeInformation;
    this.session = session;
    this.debugEnabled = debugEnabled;
    this.propertyDecryptor = propertyDecryptor;
  }

  public EmbeddedScanner create() {
    EmbeddedScanner scanner = EmbeddedScanner.create(logOutput);
    scanner.setApp("Maven", runtimeInformation.getApplicationVersion().toString());

    scanner.addGlobalProperties(createGlobalProperties());

    // Secret property to manage backward compatibility on SQ side prior to 5.2 (see ProjectScanContainer)
    scanner.setGlobalProperty("sonar.mojoUseRunner", "true");
    if (debugEnabled) {
      scanner.setGlobalProperty("sonar.verbose", "true");
    }

    return scanner;
  }

  private Properties createGlobalProperties() {
    Properties p = new Properties();
    p.putAll(session.getCurrentProject().getProperties());
    p.putAll(session.getExecutionProperties());
    p.putAll(propertyDecryptor.decryptProperties(p));
    return p;
  }
}
