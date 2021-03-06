/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.platform.plugin.output;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.event.ReportProgressEvent;
import org.pentaho.reporting.libraries.base.config.ModifiableConfiguration;
import org.pentaho.reporting.platform.plugin.MicroPlatformFactory;
import org.pentaho.reporting.platform.plugin.async.IAsyncReportListener;
import org.pentaho.reporting.platform.plugin.async.ReportListenerThreadHolder;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class EmailOutputTest {
  EmailOutput emailOutput;
  private IAsyncReportListener listener;

  @Before public void setUp() {
    emailOutput = new EmailOutput();
    listener = mock( IAsyncReportListener.class );
    ReportListenerThreadHolder.setListener( listener );
  }

  @After public void tearDown() {
    ReportListenerThreadHolder.clear();
    listener = null;
  }

  @Test public void testPaginate() throws Exception {
    assertEquals( 0, emailOutput.paginate( null, 0 ) );
  }

  @Test public void testSupportsPagination() throws Exception {
    assertEquals( false, emailOutput.supportsPagination() );
  }

  @Test public void testGetReportLock() throws Exception {
    assertEquals( emailOutput, emailOutput.getReportLock() );
  }

  @Test
  public void testGenerateListener() throws Exception {
    MicroPlatform microPlatform = MicroPlatformFactory.create();
    microPlatform.start();
    ClassicEngineBoot.getInstance().start();
    try {
      emailOutput.generate( new MasterReport(), 1, new ByteArrayOutputStream(), 1 );

      verify( listener, times( 1 ) ).reportProcessingStarted( any( ReportProgressEvent.class ) );
      verify( listener, times( 1 ) ).reportProcessingFinished( any( ReportProgressEvent.class ) );
      verify( listener, atLeastOnce() ).reportProcessingUpdate( any( ReportProgressEvent.class ) );
    } finally {
      microPlatform.stop();
      microPlatform = null;
    }

  }

  @Test
  public void testGenerate() throws Exception {
    MicroPlatform microPlatform = MicroPlatformFactory.create();
    microPlatform.start();
    ClassicEngineBoot.getInstance().start();
    ReportListenerThreadHolder.clear();
    try {
      emailOutput.generate( new MasterReport(), 1, new ByteArrayOutputStream(), 1 );

      verify( listener, times( 0 ) ).reportProcessingStarted( any( ReportProgressEvent.class ) );
      verify( listener, times( 0 ) ).reportProcessingFinished( any( ReportProgressEvent.class ) );
      verify( listener, times( 0 ) ).reportProcessingUpdate( any( ReportProgressEvent.class ) );
    } finally {
      microPlatform.stop();
      microPlatform = null;
    }
  }

  @Test
  public void testInlineStyles() throws PlatformInitializationException {
    final Map<Object, Object> configMap = new HashMap<>();
    final MasterReport report = mock( MasterReport.class );
    final ModifiableConfiguration configuration = mock( ModifiableConfiguration.class );

    doAnswer( i -> configMap.put( i.getArguments()[0], i.getArguments()[1] ) ).when( configuration ).setConfigProperty( any(), any() );

    when( report.getConfiguration() ).thenReturn( configuration );

    MicroPlatform microPlatform = MicroPlatformFactory.create();
    microPlatform.start();
    ClassicEngineBoot.getInstance().start();
    ReportListenerThreadHolder.clear();
    try {
      emailOutput.generate( report, 1, new ByteArrayOutputStream(), 1 );
    } catch ( final Exception ignored ) {
    } finally {
      assertEquals( configMap.get( "org.pentaho.reporting.engine.classic.core.modules.output.table.html.InlineStyles" ),
        "true" );
      assertEquals(
        configMap.get( "org.pentaho.reporting.engine.classic.core.modules.output.table.html.ExternalStyle" ), "false" );
      microPlatform.stop();
      microPlatform = null;
    }

  }
}

