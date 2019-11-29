--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.14
-- Dumped by pg_dump version 9.5.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: metadataschemaregistry; Type: TABLE DATA; Schema: public; Owner: dspace
--

INSERT INTO public.metadataschemaregistry (namespace, short_id) VALUES ('http://doku.b.tu-harburg.de/terms', 'tuhh'), ('http://doku.b.tu-harburg.de/thesis/', 'thesis'),('http://openaire.eu', 'openaire'), ('https://schema.datacite.org', 'datacite'), ('https://dspace-cris.4science.it', 'local');
--INSERT INTO public.metadataschemaregistry (namespace, short_id) VALUES ('http://dspace.org/bundle', 'bundle'), ('http://dspace.org/bitstream', 'bitstream');


--
-- Data for Name: metadatafieldregistry; Type: TABLE DATA; Schema: public; Owner: dspace
--

-- TOR additions to DC schema
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'relation', 'project', 'Relation to a project (eg. this item has been funded by a funding project or was created in context of a project)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'relation', 'sponsor', 'Sponsor / Funder of this document');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'identifier', 'urn', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'date', 'examination', 'Date of an oral examination to acquire doctor title for a thesis');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'type', 'thesis', 'Thesis type (level) for thesis documents');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'type', 'dini', 'DINI publication type accoding to urn:nbn:de:kobv:11-100109998');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'bcl', 'Basic Classification of Common Library Network Germany Class');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'gnd', 'GND class');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'bclcode', 'BCL number only');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'ddccode', 'DDC class number only');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'msc', 'Complete MSC Class');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'msccode', 'MSC Code');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'pacs', 'PACS class');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'pacscode', 'PACS code');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'ccs', 'CCS class');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'ccscode', 'CCS class code');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'subject', 'gndcode', 'Numeric GND code');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'contributor', 'department', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'identifier', 'hdl', 'Handle of this record');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'type', 'driver', 'Documenttype according to DRIVER vocabulary');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'identifier', 'oclc', 'OCLC number');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'rights', 'cc', 'Name of the CC License');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'rights', 'ccversion', 'Version of the CC license');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'type', 'rdm', 'Type of Research Data');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'type', 'casrai', 'Document type according to CASRAI vocabulary (http://dictionary.casrai.org/Output_Types), needed for DataCite''s document type element.');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (2, 'DCMIType', NULL, 'DCMI type of the resource.');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (3, 'ou', NULL, 'Organizational Unit this person is associated with');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'identifier', 'urn', 'Von der TUHH vergebener URN');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publikation', 'typ', 'Typ der Publikation nach DINI-Typisierung.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publikation', 'source', 'Gedruckter Nachweis der Publikation.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publikation', 'fachbereich', 'Arbeitsbereich der TUHH, an dem das Dokument entstanden ist.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'opus', 'id', 'OPUS3-ID des Dokuments.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'gnd', NULL, 'GND-Klassen, kommasepariert');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'bkl', NULL, 'BKL-Klasse für dieses Dokument');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'gvk', 'ppn', 'PPN im GVK');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'oai', 'show', 'Nur Datensätze mit diesem Element werden über OAI angezeigt. Der Wert des Elements ist egal.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'pod', 'url', 'Link to buy this item as Print-on-demand-book');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'pod', 'allowed', 'Boolean, whether pod is allowed for this document or not');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'date', 'embargolift', 'Embargo-Lift-Date');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publisher', 'note', 'Label for the link to a parallel publication');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publisher', 'uri', 'Link to the publisher''s version of this document');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'title', 'german', 'Deutscher Titel des Dokuments');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'title', 'english', 'Englischsprachiger Titel des Dokuments');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'abstract', 'german', 'Deutschsprachiger Abstract des Dokuments');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'abstract', 'english', 'Englischsprachiger Abstract des Dokumentes');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'relation', 'ispartof', 'Journalname, in dem ein Artikel erschienen ist. Kann mitsamt ISSN als Authority in das Feld dc.relation.ispartof übernommen werden.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'editor', 'corporate', 'Herausgebende Körperschaft');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'contributor', 'corporate', 'Sonstwie beteilgte Körperschaft');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publisher', 'doi', 'DOI der Verlagspublikation (bei Parallelpublikation)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publisher', 'url', 'URL der Verlagspublikation (bei Parallelpublikation)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'sponsor', 'doi', 'Fundref DOI der fördernden Körperschaft (nach der Liste http://fundref.org/fundref/fundref_registry.html)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'publication', 'institute', 'Institute associated with this publication');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'identifier', 'doi', 'Von der TUHH vergebener DOI');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'type', 'opus', 'Aus OPUS importierter Dokumenttyp (fürs Browsing)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'uploader', 'email', 'EMailadresse der Person, die das Dokument originär eingebracht hat.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'contributor', 'originator', 'Aus OPUS importierter Wert für Urheber');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'note', 'intern', 'Von OPUS übernommene Bemerkung für intern');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'note', 'extern', 'Von OPUS übernommene Bemerkung für extern');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'institute', 'german', 'Deutsche Institutsbezeichnung');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'institute', 'english', 'Englische Institutsbezeichnung');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'institute', 'id', 'ID des publizierenden Instituts');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'type', 'id', 'Interne ID des Dokumenttyps');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'gvk', 'hasppn', 'Wenn false, hat dieser Datensatz noch keine PPN im GVK');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'contributor', 'advisor', 'Gutachter einer Arbeit an der tuhh');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'contributor', 'referee', 'Gutachter einer Abschlussarbeit (Zweitbetreuer)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'hasurn', NULL, 'Ein von der TUHH vergebener URN liegt vor');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'series', 'id', 'Interne ID einer Schriftenreihe');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'series', 'name', 'Name der Schriftenreihe (fürs Browsing)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'doi', 'url', 'DOI with resolver');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'embargo', 'date', 'Embargodatum');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'embargo', 'reason', 'Optionale Begründung für das Embargo');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'date', 'embargo', 'Embargodate (veraltet, bitte tuhh.embargo.date nutzen!)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'container', 'issue', 'Issue der Zeitschrift, in der der Artikel erschienen ist');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'container', 'volume', 'Volume der Zeitschrift, in der der Artikel erschienen ist');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'container', 'startpage', 'Seite, auf der Artikel oder das Kapitel im enthaltenden Container beginnt');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'container', 'endpage', 'Seite, auf der Artikel oder das Kapitel im enthaltenden Container endet');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'relation', 'publication', 'Bezug zu anderer Veröffentlichung auf tub.dok');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'relation', 'haspart', 'Enthält andere Titel auf tub.dok');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'relation', 'issupplementedby', 'Lokales Behelfsfeld für DataCite zur Verknüpfung mit Forschungsdaten, die in dieser Publikation benutzt werden');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'relation', 'ispartofseriesnumber', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'relation', 'ispartofseries', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'subject', 'fieldofcompetence', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'subject', 'researchfocus', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='tuhh'), 'type', 'rdm', 'Boolean value if this is research data or not');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='thesis'), 'grantor', NULL, 'Information about the grantor of a thesis.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='thesis'), 'grantor', 'universityOrInstitution', 'Name of the granting university or institution');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='thesis'), 'grantor', 'place', 'Place of residence for the granting university or institution');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='thesis'), 'grantor', 'department', 'Department where the thesis was written');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='thesis'), 'type', NULL, 'Type (i.e. level) of thesis.');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='openaire'), 'rights', NULL, 'OpenAire Access Rights Specification, needs to be mapped to dc.rights for OAI');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='openaire'), 'funder', 'name', 'Name of the funder of this project, needs to be the official OpenAIRE abreviation');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='openaire'), 'funder', 'programme', 'Name of the funding programme, needs to be the official OpenAire code (e.g. H2020 for Horizon2020)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='openaire'), 'funder', 'projectid', 'Project ID for the project this publication has been funded with');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='openaire'), 'funder', NULL, 'Complete funder reference according to OpenAire guidelines');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'Cites', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsCitedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsSupplementedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsSupplementTo', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsReferencedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'References', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsPreviousVersionOf', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsNewVersionOf', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'HasPart', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsPartOf', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'Documents', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsDocumentedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsCompiledBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'Compiles', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'relation', 'IsIdenticalTo', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'contributor', 'ContactPerson', 'Person with knowledge of how to access, troubleshoot, or otherwise field issues related to the resource');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'contributor', 'DataCollector', 'Person/institution responsible for finding, gathering/collecting data under the guidelines of the author(s) or Principal Investigator (PI)');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'contributor', 'Researcher', 'A person involved in analyzing data or the results of an experiment or formal study. May indicate an intern or assistant to one of the authors who helped with research but who was not so “key” as to be listed as an author.');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'contributor', 'DataCurator', 'Person tasked with reviewing, enhancing, cleaning, or standardizing metadata and the associated data submitted for storage, use, and maintenance within a data centre or repository');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='datacite'), 'contributor', 'Other', 'Any person or institution making a significant contribution to the development and/or maintenance of the resource, but whose contribution does not “fit” other controlled vocabulary for contributorType.');

INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'message', 'claim', 'Message about claiming action');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'identifier', 'external', NULL);
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'contributorCorporate', 'editor', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (1, 'contributorCorporate', 'other', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsCitedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsCompiledBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsDocumentedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsIdenticalTo', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsNewVersionOf', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsPartOf', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsPreviousVersionOf', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsReferencedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsSupplementedBy', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'IsSupplementTo', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'References', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'Cites', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'Compiles', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'Documents', '');
INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES ((SELECT metadata_schema_id FROM public.metadataschemaregistry WHERE short_id='local'), 'relation', 'HasPart', '');


--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (226, 8, 'viewer', 'provider', 'Metadata field used to register custom viewer');
--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (227, 8, 'master', NULL, 'Metadata field used to link an alternative (mostly
--					for access) copy to the master one');
--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (228, 8, 'image', 'height', 'Metadata field used to store the height of the image');
--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (229, 8, 'image', 'width', 'Metadata field used to store the width of the image');
--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (230, 8, 'image', 'thumbnail', 'Metadata field used to store the ID of the thumbnail image');
--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (231, 8, 'image', 'preview', 'Metadata field used to store the ID of the preview image');
--INSERT INTO public.metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note) VALUES (232, 8, 'ckan', 'resourceid', 'Metadata field used for the resource ID of the file in CKAN');



--
-- PostgreSQL database dump complete
--
