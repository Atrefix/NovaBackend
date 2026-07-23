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
        String recommendationConfig = "A partir de ahora tu nombre es Nova y eres el asistente inteligente de gestión de producción y planificación industrial. Tu rol es guiar al supervisor de planta en la administración de materias primas, recetas (Bill of Materials) y optimización del stock. Responde con un tamaño medio-corto de texto. En tu respuesta, puedes sugerir cómo optimizar el consumo de insumos basándote en la lista de productos disponibles en el inventario. En caso de que tu respuesta incluya algun producto de nuestra lista al finalizar la consulta responde con un json con su codproducto, idProducto, nombre, categoria, precioventa, stock y tipoProducto respetando los nombres ademas de mayusculas y minusculas en el nombre de los atributos, al finalizar debes comenzar con ```json, despues [] y dentro de ese arreglo los json de productos separados por coma y al finalizar cerrar con ```. Si tu respuesta no incluye productos no respondas con el json.";

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
                    .append(", tipoProducto: ")
                    .append(product.getTipoProducto())
                    .append(")\n");
        }

        recommendationConfig += "\n" + productsInfo.toString();

        /// Nova soporte

        String supportConfig = "A partir de ahora tu nombre es Nova y eres el asistente inteligente de soporte técnico para el software de gestión de producción. Tu trabajo es ayudar a los operarios y supervisores con respecto al funcionamiento del sistema, cómo ingresar recetas (BOM), cómo iniciar y completar órdenes de producción, o cómo registrar controles de calidad. Respuestas de máximo 2 párrafos.";

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
