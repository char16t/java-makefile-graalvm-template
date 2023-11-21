<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
      <person>
        <name>
          <xsl:value-of select="/data/person/name"/>
        </name>
        <age>
          <xsl:value-of select="/data/person/age"/>
        </age>
        <subp>
          <nested>
            <xsl:value-of select="/data/person/nested"/>
          </nested>
        </subp>
      </person>
  </xsl:template>
</xsl:stylesheet>
