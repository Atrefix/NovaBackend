package com.cursoIntegrador.novaBackend.Service.IA;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.Entity.Product;
import com.cursoIntegrador.novaBackend.Service.DAO.ProductService;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import jakarta.annotation.PostConstruct;

@Service
@PropertySource("classpath:secret-credentials.properties")
public class GeminiService {

    @Value("${gemini.api-key}")
    private String APIKey;

    @Value("${gemini.model-id}")
    private String modelId;

    private Client client;

    private GenerateContentConfig recConfig;
    private GenerateContentConfig supConfig;

    private Chat chatSessionRec;
    private Chat chatSessionSup;

    @Autowired
    ProductService productService;

    @PostConstruct
    public void init() {
        this.client = Client.builder().apiKey(APIKey).build();

        /// Información de productos

        List<Product> products = productService.getAllProducts();
        StringBuilder productsInfo = new StringBuilder("Nuestros productos disponibles son:\n");
        String recommendationConfig = "A partir de ahora tu nombre es Nova y eres el asistente inteligente de una cafeteria llamado novaBackend. Responde con un tamaño medio-corto de texto. Si el usuario pregunta algo fuera del tema de la cafeteria, desvía suavemente la conversación hacia productos, servicios o promociones del local. En caso de que tu respuesta incluya algun producto de nuestra lista al finalizar la consulta responde con un json con su codproducto, idProducto, nombre, categoria, precioventa y stock respetando los nombres ademas de mayusculas y minusculas en el nombre de los atributos, al finalizar debes comenzar con ```json, despues [] y dentro de ese arreglo los json de productos separados por coma y al finalizar cerrar con ```. Si tu respuesta no incluye productos no respondas con el json.";

        for (Product product : products) {
            productsInfo.append("- ")
                    .append(product.getNombre())
                    .append(" (categoria: ")
                    .append(product.getCategoria())
                    .append(", codproducto: ")
                    .append(product.getCodproducto())
                    .append(", idProducto: ")
                    .append(product.getIdproducto())
                    .append(", stock: ")
                    .append(product.getStock())
                    .append(", preciocompra: $")
                    .append(product.getPreciocompra())
                    .append(", precioventa: $")
                    .append(product.getPrecioventa())
                    .append(")\n");
        }

        recommendationConfig += "\n" + productsInfo.toString();

        /// Nova soporte

        String supportConfig = "A partir de ahora tu nombre es Nova y eres el asistente inteligente de una cafeteria llamado novaBackend, tu trabajo es ayudar al usuario segun las preguntas que haga con respecto a la cafeteria con respuestas de maximo 2 parrafos, ya sea de que no sabe si la pagina web tiene la capacidad de hacer algo, o sobre los medios de pago. Si el usuario pregunta algo fuera del tema anteriormente mencionado, desvía suavemente la conversación hacia temas de la cafeteria o servicios del local.";

        /// Fin Nova soporte

        this.recConfig = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(recommendationConfig))).build();

        this.supConfig = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(supportConfig))).build();

        this.chatSessionRec = client.chats.create(modelId, recConfig);
        this.chatSessionSup = client.chats.create(modelId, supConfig);
    }

    public String novaPromptCompuesto(String prompt, String mode) {

        GenerateContentResponse response;

        if (mode.equalsIgnoreCase("recommendations")) {
            response = chatSessionRec.sendMessage(prompt);
            return response.text();
        } else {
            response = chatSessionSup.sendMessage(prompt);
            return response.text();
        }

    }

}
