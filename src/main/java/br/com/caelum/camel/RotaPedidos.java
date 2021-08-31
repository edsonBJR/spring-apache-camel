package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				// aqui estamos utilizado file do camel, com delay de 5 segundos e não apagando os arquivos da origem
				from("file:pedidos?delay=5s&noop=true").
					setProperty("pedidoId", xpath("/pedido/id/text()")).
					setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()")).
					// Aqui estamos dividindo o item em mensagens de modo que consigamos pegar a mensagem com o elemento EBOOK
					split().
						xpath("/pedido/itens/item").
					// Aqui estamos utilizando a linguagem xpath para navegar no elemento EBOOK que está dentro do nosso xml
					filter().
						xpath("/item/formato[text()='EBOOK']").
						setProperty("ebookId", xpath("/item/livro/codigo/text()")).
					// aqui estamos exibindo no console a informação de log da mensagem que o camel irá transmitir
					log("${id}").
					// aqui estamos com o método marshal transformando de xml para json
					marshal().xmljson().
					// aqui estamos vendo o conteúdo em json
					log("${id} - ${body}").
					// aqui estamos alterando a extensão do arquivo utilizando o compente CamelFileName, com o método simple passamos uma expressão, para aproveitar os nomes dos arquivos sem a extensão mas acrescentando a extensão.
//					setHeader("CamelFileName", simple("${file:name.noext}.json")).
//					setHeader(Exchange.FILE_NAME, simple("${file:name.noext}-${header.CamelSplitIndex}.json")).
//					setHeader(Exchange.HTTP_METHOD, HttpMethods.POST).
					setHeader(Exchange.HTTP_METHOD, HttpMethods.GET).
//					setHeader(Exchange.HTTP_QUERY, constant("ebookId=ARQ&pedidoId=2451256&clienteId=edgar.b@abc.com")).
					setHeader(Exchange.HTTP_QUERY, simple("ebookId=${property.ebookId}&pedidoId=${property.pedidoId}&clienteId=${property.clienteId}")).
					// aqui estamos gravando o json transformando dentro da pasta saida	
//				to("file:saida"); 
				to("http4://localhost:8081/webservices/ebook/item");
			}
		});
		context.start();
		Thread.sleep(20000);
		context.stop();
	}	
}
