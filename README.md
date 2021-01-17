# Energy ADE extension for the 3D City Database
This is a 3DCityDB extension for the **Energy Application Domain Extension** (Energy ADE) for CityGML.
The Energy ADE extends CityGML by features and properties necessary to perform energy simulations and to store and
exchange the corresponding results.

This extension adds support for managing Energy ADE data inside the 3DCityDB and enables the
[Importer/Exporter tool](https://github.com/3dcitydb/importer-exporter) to load and export Energy ADE enriched datasets.
The support is tailored to the **Karlsruhe Institute of Technology (KIT) profile of the Energy ADE version 1.0**.

* **Energy ADE specification and material: http://www.citygmlwiki.org/index.php/CityGML_Energy_ADE**
* **KIT profile of the Energy ADE v1.0: http://www.citygmlwiki.org/images/4/41/KIT-UML-Diagramme-Profil.pdf**  
* **Where to file issues: https://github.com/3dcitydb/energy-ade-citydb/issues**

## How to use this extension
The Energy ADE extension consists of two main parts:

- A relational schema mapping the KIT profile of the Energy ADE data model to a set of database tables and objects. The
  relational schema is built upon and seamlessly integrates with the official 3DCityDB relational schema.
- Java modules that are automatically loaded by the Importer/Exporter and enable the tool to read/write Energy
  ADE enriched datasets and to store and manage Energy ADE data in the 3DCityDB.

Before using the Energy ADE extension, you must set up an instance of the 3DCityDB and install the Importer/Eporter tool.
Please follow the installation guidelines provided in the
[3DCityDB online documentation](https://3dcitydb-docs.readthedocs.io/en/latest/intro/index.html). You will need the
[ADE Manager Plugin](https://3dcitydb-docs.readthedocs.io/en/latest/impexp/plugins/ade-manager.html) of the
Importer/Exporter. So, make sure to select this plugin in the setup wizard of the Importer/Exporter.

Afterwards, download a release package of the Energy ADE extension as ZIP file from the
[releases section](https://github.com/3dcitydb/energy-ade-citydb/releases). Please check the release information to
make sure that the extension can be used with your version of the 3DCityDB and of the Importer/Exporter tool.
Unzip the package into the `ade-extensions` folder inside the installation directory of the Importer/Exporter.

Next, create and register the Energy ADE relational schema in your 3DCityDB instance. The easiest
way to do this is to use the ADE Manager Plugin and the contents of the unzipped release package.
Follow the steps described in the 3DCityDB online documentation:

* **[How to register the Energy ADE schema in your 3DCityDB instance](https://3dcitydb-docs.readthedocs.io/en/latest/impexp/plugins/ade-manager.html#user-interface)**

You have to register the schema only once in every 3DCityDB instance that should be able to manage Energy ADE data.

Finally, use the Importer/Exporter to connect to your Energy ADE extended 3DCityDB instance and load/extract energy data.
If you have correctly unzipped the Energy ADE extension package inside the `ade-extensions` folder, the tool will
automatically detect the extension and will be able to handle Energy ADE datasets. Again, the main steps for using an
ADE extension with the Importer/Exporter are described in the 3DCityDB online documentation:

* **[How to use the Energy ADE extension with the Importer/Exporter](https://3dcitydb-docs.readthedocs.io/en/latest/impexp/plugins/ade-manager.html#workflow-of-extending-the-import-export-tool)**

Simple datasets for testing the Energy ADE extension can be found in the [resources/datasets](https://github.com/3dcitydb/energy-ade-citydb/tree/main/resources/datasets)
folder of this repository.

## Technical details
The relational schema for the Energy ADE has been fully automatically derived from the XML schemas using the ADE Manager
Plugin of the Importer/Exporter. This ADE-to-3DCityDB mapping should work for all CityGML ADE XML schemas and is documented
[here](https://3dcitydb-docs.readthedocs.io/en/latest/impexp/plugins/ade-manager.html#workflow-of-extending-the-import-export-tool).

The Java module for enabling the Importer/Exporter tool to store Energy ADE data according to the extended relational schema
has been manually implemented against the `ADEExtension` interface and plugin mechanism of the Importer/Exporter.
The `ADEExtension` interface has been introduced with [version 4.0](https://github.com/3dcitydb/3dcitydb/releases/tag/v4.0.0)
of the 3D City Database. The [TestADE repository](https://github.com/3dcitydb/extension-test-ade) demonstrates the
implementation of an artificial ADE and may serve as template for implementing extensions for your own ADEs.

The Java module for parsing and writing Energy ADE enriched datasets is implemented as extension for the open source
CityGML library citygml4j. More information can be found [here](https://github.com/citygml4j/energy-ade-citygml4j).

## Building from source
The Energy ADE 3DCityDB extension uses [Gradle](https://gradle.org/) as build system. To build the extension from source,
clone the repository to your local machine and run the following command from the root of the repository.

    > gradlew installDist

The script automatically downloads all required dependencies for building the module. So make sure you are connected
to the internet. The build process runs on all major operating systems and only requires a Java 8 JDK or higher to run.

If the build was successful, you will find the extension package of the Energy ADE module under `energy-ade-citydb/build/install`.

## About the Energy ADE 3DCityDB extension
This extension was initially developed and brought to the 3DCityDB open source project by [Virtual City Systems](https://vc.systems/)
and the [Institute for Automation and Applied Informatics (IAI) at Karlsruhe Institute of Technology (KIT)](https://www.iai.kit.edu/). 

The Energy ADE is an Application Domain Extension for [OGC CityGML 2.0](http://www.opengeospatial.org/standards/citygml).

# Publications
- [Agugiaro, G. et al.: The Energy Application Domain Extension for CityGML: enhancing interoperability for urban energy
  simulations, Open Geospatial Data, Software and Standards (2018) 3:2](https://doi.org/10.1186/s40965-018-0042-y)
- [Benner, J. et al.: Virtual 3D City Model Support for Energy Demand Simulations on City Level - The CityGML Energy
  Extension, Proceedings REAL CORP Conference 2016, Hamburg, 22 - 24 June 2016](http://conference.corp.at/archive/CORP2016_20.pdf)
- [Nouvel, R. et al.: Genesis of the CityGML Energy ADE, Proceedings of the CISBAT International Conference 2015 –
  Lausanne – Switzerland](http://infoscience.epfl.ch/record/213436/files/9_NOUVEL1109.pdf)

## License

The Energy ADE 3DCityDB extension is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
See the `LICENSE` file for more details.
