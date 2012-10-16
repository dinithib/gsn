#! /usr/bin/python
# -*- coding: UTF-8 -*-
__author__      = "David Hasenfratz <hasenfratz@tik.ee.ethz.ch>"
__copyright__   = "Copyright 2011, ETH Zurich, Switzerland, David Hasenfratz"
__license__     = "GPL"
__version__     = "$Revision: 3717 $"
__date__        = "$Date: 2011-10-13 09:16:45 +0200 (Thu, 13 Oct 2011) $"
__id__          = "$Id: GPSPluginNAV.py 3717 2011-10-13 07:16:45Z dhasenfratz $"

'''
backlog imports
'''
import BackLogMessage
from AbstractPlugin import AbstractPluginClass
import time
from threading import Event

import GPSDriverNAV
import GsmScanner
'''
defines
'''

DEFAULT_BACKLOG = True

class GsmPluginClass(AbstractPluginClass):

    def __init__(self, parent, config):

        AbstractPluginClass.__init__(self, parent, config, DEFAULT_BACKLOG)
        self._sleeper = Event()
        self._stopped = False
        self._pollInterval = float(self.getOptionValue('poll_interval'))

        self.info('Init GsmPlugin...')

        self._gpsDeviceStr = self.getOptionValue('gps_device')
        self.gps = GPSDriverNAV.GPSDriverNAV([self._gpsDeviceStr])

        self._gsmDeviceStr = self.getOptionValue('gsm_device')
        self.gsm = GsmScanner.GsmScanner([self._gsmDeviceStr])

        self.info("Done init")

    def isBusy(self):
        return False

    def run(self):
        try:
          self.name = 'Gsmplugin-Thread'
          self.debug('Gsmplugin started...')
          t = time.time()

          while not self._stopped:
              self._sleeper.wait(self._pollInterval - (time.time() - t))
              if self._sleeper.isSet():
                  continue
              t = time.time()
              self.action()
          self.info('died')
        except Exception as e:
          self.error( "Exception: " + str(e))
          self.error("Could not execute run")
          return

    def action(self):

        # Read message
        gsmMsg = ''
        self.debug('start GSM scan')
        [gsmMsg, gsmMsgList] = self.gsm.scan()
        self.debug('GSM scan completed')

        if gsmMsg != '' and gsmMsg != None:
          self.debug(gsmMsg)

          # Read GPS message
          gpsMsg = self.gps._read()
          
          if gpsMsg != 0 and gpsMsg != '' and gpsMsg is not None:
          
            # Parse message
            dataPackage = self._parseGPSMsg(gpsMsg)
            
            if dataPackage == '':
                self.warn('Could not parse GPS reading')
                return
            
            self.debug('GPS reading done')

            dataPackage += [gsmMsg]
            self.debug('Send complete msg')
            self.processMsg(self.getTimeStamp(), dataPackage)
          else:
              self.warning('No GPS data')

        else:
          self.warning('No GSM data')


    def remoteAction(self, parameters):
        return

    def stop(self):
        self._stopped = True
        self.info('stopped')

    def _parseGPSMsg(self, msg):
        if msg != 0:
            dataPackage = []
            for i in range(1, len(msg)):
                dataPackage += [msg[i]]

            self.debug(dataPackage)
            return dataPackage

        else:
            self.debug("WARNING: GPS MSG packet was empty!")
