/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2014, Ecole Polytechnique Federale de Lausanne (EPFL)
* 
* This file is part of GSN.
* 
* GSN is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* GSN is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with GSN.  If not, see <http://www.gnu.org/licenses/>.
* 
* File: src/gsn/simulation/SimHttpListener.java
*
* @author Ali Salehi
*
*/

package gsn.simulation;

import gsn.http.WebConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SimHttpListener extends HttpServlet {
   
   private static final int START_PORT_INDEX = 29000;

   private transient File         outputLog = null;
   
   private OutputStream           dos       = null;
   
   private final transient Logger logger    = LoggerFactory.getLogger( SimHttpListener.class );
   
   public void doPost ( HttpServletRequest req , HttpServletResponse res ) throws ServletException , IOException {
      int requestType = Integer.parseInt( ( String ) req.getHeader( WebConstants.REQUEST ) );
      switch ( requestType ) {
         case WebConstants.DATA_PACKET :
            res.setHeader( WebConstants.RESPONSE_STATUS , WebConstants.REQUEST_HANDLED_SUCCESSFULLY );
            if ( req.getLocalPort( ) == ( START_PORT_INDEX + 1 ) ) {
               if ( outputLog == null ) {
                  outputLog = new File( "SuperLight-ReceivedTimes.log" );
                  try {
                     dos = ( new FileOutputStream( outputLog ) );
                  } catch ( FileNotFoundException e1 ) {
                     logger.error( "Logging the fail failed" , e1 );
                     return;
                  }
               }
               try {
                  logger.info( "Data received for a typical client" );
                  dos.write( new StringBuffer( ).append( System.currentTimeMillis( ) ).append( '\n' ).toString( ).getBytes( ) );
                  dos.flush( );
               } catch ( IOException e ) {
                  logger.error( "Logging the fail failed" , e );
                  return;
               }
            }
            logger.debug( "Data Received" );
            break;
         
      }
      
   }
   
}
