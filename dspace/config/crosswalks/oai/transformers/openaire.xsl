<?xml version="1.0" encoding="UTF-8"?>
<!-- 

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

	Developed by DSpace @ Lyncode <dspace@lyncode.com> 
	Following OpenAIRE Guidelines 1.1:
		- http://www.openaire.eu/component/content/article/207

 -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:doc="http://www.lyncode.com/xoai">
	<xsl:output indent="yes" method="xml" omit-xml-declaration="yes" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

 	<!-- Formatting dc.date.issued -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field/text()">
		<xsl:call-template name="formatdate">
			<xsl:with-param name="datestr" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<!-- Removing other dc.date.* -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name!='issued']" />
	
	<!-- Prefixing dc.type -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name!='dini']" />

	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='dini']/doc:element/doc:field/text()">
		<xsl:call-template name="addPrefix">
			<xsl:with-param name="value" select="." />
			<xsl:with-param name="prefix" select="'info:eu-repo/semantics/'"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
<!--
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field/text()">
                <xsl:choose>
                <xsl:when test="contains(., 'Thesis')">
                    <xsl:call-template name="addPrefix">
                        <xsl:with-param name="value" select="/doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='thesis']" />
                        <xsl:with-param name="prefix" select="'info:eu-repo/semantics/'"></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
		<xsl:call-template name="addPrefix">
			<xsl:with-param name="value" select="." />
			<xsl:with-param name="prefix" select="'info:eu-repo/semantics/'"></xsl:with-param>
		</xsl:call-template>
                </xsl:otherwise>
                </xsl:choose>
	</xsl:template>
-->
	<!-- Prefixing and Modifying dc.rights -->
	<!-- Removing unwanted -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:element" />
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field[not (contains(., 'open access') or contains(., 'openAccess') or contains(., 'restrictedAccess') or contains(., 'embargoedAccess'))]" />

	<!-- Replacing -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field/text()">
		<xsl:choose>
			<xsl:when test="contains(., 'open access')">
				<xsl:text>info:eu-repo/semantics/openAccess</xsl:text>
			</xsl:when>
			<xsl:when test="contains(., 'openAccess')">
				<xsl:text>info:eu-repo/semantics/openAccess</xsl:text>
			</xsl:when>
			<xsl:when test="contains(., 'restrictedAccess')">
				<xsl:text>info:eu-repo/semantics/restrictedAccess</xsl:text>
			</xsl:when>
			<xsl:when test="contains(., 'embargoedAccess')">
				<xsl:text>info:eu-repo/semantics/embargoedAccess</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>info:eu-repo/semantics/restrictedAccess</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

        <!-- Adding funding information -->
        <xsl:template match="/doc:metadata/doc:element[@name='openaire']/doc:element[@name='funder']/doc:element[@name='name']/doc:element/doc:field/text()">
                <xsl:call-template name="fundinginfo">
                    <xsl:with-param name="funderName" select="." />
                    <xsl:with-param name="funderProgramme" select="../../../../doc:element[@name='programme']/doc:element/doc:field/text()" />
                    <xsl:with-param name="funderProjectId" select="../../../../doc:element[@name='projectid']/doc:element/doc:field/text()" />
                </xsl:call-template>
        </xsl:template>

        <!-- Removing dc.relations except for funding information -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:element/doc:field[not (contains(., 'info:eu-repo/grantAgreement'))]" />
<!--
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='dini']/doc:element/doc:field/text()">
		<xsl:call-template name="addPrefix">
			<xsl:with-param name="value" select="." />
			<xsl:with-param name="prefix" select="'info:eu-repo/semantics/'"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
-->
	<!-- AUXILIARY TEMPLATES -->
	
	<!-- dc.type prefixing -->
	<xsl:template name="addPrefix">
		<xsl:param name="value" />
		<xsl:param name="prefix" />
		<xsl:choose>
			<xsl:when test="starts-with($value, $prefix)">
				<xsl:value-of select="$value" />
			</xsl:when>
			<xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="contains($value, 'PeriodicalPart')">
                                    <xsl:value-of select="concat($prefix, 'contributionToPeriodical')" />
                                </xsl:when>
                                <xsl:when test="contains($value, 'Other')">
                                    <xsl:value-of select="concat($prefix, 'other')" />
                                </xsl:when>
                                <xsl:when test="contains($value, 'CourseMaterial')">
                                    <xsl:value-of select="concat($prefix, 'other')" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat($prefix, $value)" />
                                </xsl:otherwise>
                            </xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Date format -->
	<xsl:template name="formatdate">
		<xsl:param name="datestr" />
		<xsl:variable name="sub">
			<xsl:value-of select="substring($datestr,1,10)" />
		</xsl:variable>
		<xsl:value-of select="$sub" />
	</xsl:template>

        <!-- Funding information -->
        <xsl:template name="fundinginfo">
                <xsl:param name="funderName" />
                <xsl:param name="funderProgramme" />
                <xsl:param name="funderProjectId" />
                <xsl:text>info:eu-repo/grantAgreement/</xsl:text>
                <xsl:value-of select="$funderName" /><xsl:text>/</xsl:text>
                <xsl:value-of select="$funderProgramme" /><xsl:text>/</xsl:text>
                <xsl:value-of select="$funderProjectId" />
        </xsl:template>
</xsl:stylesheet>
