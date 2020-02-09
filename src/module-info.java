module network_analyzer {
	requires jcommander;
	requires java.xml;
	requires java.base;
	requires org.apache.logging.log4j;
	opens de.frauas.group13 to jcommander;
}