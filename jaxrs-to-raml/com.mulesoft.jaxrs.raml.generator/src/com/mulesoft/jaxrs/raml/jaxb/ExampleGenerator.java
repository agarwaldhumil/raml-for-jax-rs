package com.mulesoft.jaxrs.raml.jaxb;

import java.util.HashMap;
import java.util.HashSet;

public class ExampleGenerator {

	public ExampleGenerator(IExampleWriter writer) {
		super();
		this.writer = writer;
	}

	protected IExampleWriter writer;
	
	public void generateXML(JAXBType type){
		String xmlName = type.getXMLName();
		generateType(type, xmlName);
	}

	HashSet<JAXBType>onStack=new HashSet<JAXBType>();
	
	private void generateType(JAXBType type, String xmlName) {
		if(!onStack.add(type)){
			return;
		}
		HashMap<String,String>prefixes=type.gatherNamespaces();
		writer.startEntityAndDeclareNamespaces(xmlName,prefixes);		
		for (JAXBProperty p:type.properties){
			writeProperty(p,prefixes);
		}
		writer.endEntity(xmlName);
		onStack.remove(type);
		
	}

	private void writeProperty(JAXBProperty p, HashMap<String, String> prefixes) {
		String name=p.name();
		if (name==null||name.length()==0){
			return;
		}
		if (p.namespace!=null){
			String string = prefixes.get(p.namespace);
			if (string!=null){
				name=string+":"+name;
			}
		}
		if (p instanceof JAXBAttributeProperty){
			JAXBAttributeProperty ap=(JAXBAttributeProperty) p;
			writer.generateAttribute(name, ap.asJavaType(), ap.required);
		}
		if (p instanceof JAXBElementProperty){
			JAXBElementProperty el=(JAXBElementProperty) p;
			JAXBType jaxbType = el.getJAXBType();
			if (jaxbType!=null){
				generateType(jaxbType,name);
				if(p.isCollection()){
					generateType(jaxbType,name);
				}
			}
			else{
				writer.generateElement(name, el.asJavaType(), el.required);
				if(p.isCollection()){
					writer.generateElement(name, el.asJavaType(), el.required);					
				}
			}
			//writer.generateAttribute(p.getElementName(), type);
		}
		if (p instanceof JAXBValueProperty){
			writer.addValueSample(p.asJavaType(), p.required);
			//writer.generateAttribute(p.getElementName(), type);
		}
	}
}
