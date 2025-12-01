import { useEffect, useRef, useState, type FormEvent, type JSX } from "react";
import { OverlayTrigger, Popover } from "react-bootstrap";
import "./AgentIA.css";
import "@google/model-viewer";
import { RequestIAConsulta, type AgentIAMode } from "../api/IAApi";
import CartStore from "../stores/CartStore";
import ToastStore from "../stores/ToastStore";

type IAProduct = {
    idproducto: number;
    codproducto: string;
    nombre: string;
    categoria?: string;
    precioventa: string | number;
    stock?: number;
};

type ChatMessage = {
    id: string;
    role: "user" | "assistant";
    content: string;
    products?: IAProduct[];
};

type AgentIAProps = {
    mode?: AgentIAMode | "Support";
};

type PochiMood = "idle" | "happy" | "emotional";
type IdleVariant = "float" | "wiggle";

const WELCOME_MESSAGES: Record<AgentIAMode, string> = {
    recommendations:
        "Hola, soy Pochi 🧁. Puedo sugerirte productos según lo que se te antoje o lo que ya tienes en el carrito. ¡Cuéntame qué te provoca!",
    Support:
        "Hola, soy Pochi 🧁. Estoy aquí para ayudarte con dudas sobre tu pedido, el estado de tu compra o el uso de la página.",
};

function getProductImage(product: IAProduct): string {
    return `http://localhost:8080/images/productos/${encodeURIComponent(product.codproducto)}.webp`;
}

function extractProductsFromAnswer(text: string): { cleanText: string; products?: IAProduct[] } {
    const regex = /```json([\s\S]*?)```/i;
    const match = text.match(regex);
    if (!match) return { cleanText: text };

    const jsonText = match[1].trim();
    try {
        const parsed = JSON.parse(jsonText);
        const arr = (Array.isArray(parsed) ? parsed : [parsed]) as IAProduct[];
        const products = arr.filter((p) => p && typeof p.codproducto === "string");
        const cleanText = text.replace(match[0], "").trim();
        return { cleanText, products };
    } catch {
        return { cleanText: text };
    }
}

function renderFormattedText(text: string) {
    const parts: JSX.Element[] = [];
    const regex = /\*\*(.+?)\*\*/g;
    let lastIndex = 0;
    let key = 0;
    let match: RegExpExecArray | null;

    while ((match = regex.exec(text)) !== null) {
        if (match.index > lastIndex)
            parts.push(<span key={key++}>{text.slice(lastIndex, match.index)}</span>);
        parts.push(<strong key={key++}>{match[1]}</strong>);
        lastIndex = match.index + match[0].length;
    }

    if (lastIndex < text.length) parts.push(<span key={key++}>{text.slice(lastIndex)}</span>);

    return parts;
}

function AgentIA({ mode = "recommendations" }: AgentIAProps) {
    const normalizedMode = (mode === "Support" ? "Support" : mode) as AgentIAMode;
    const [messages, setMessages] = useState<ChatMessage[]>([
        {
            id: "welcome",
            role: "assistant",
            content: WELCOME_MESSAGES[normalizedMode],
        },
    ]);
    const [input, setInput] = useState("");
    const [isSending, setIsSending] = useState(false);
    const [pochiMood, setPochiMood] = useState<PochiMood>("idle");
    const [idleVariant, setIdleVariant] = useState<IdleVariant>("float");
    const showToast = ToastStore((state) => state.showToast);

    const messagesRef = useRef<HTMLDivElement | null>(null);
    const muffinRef = useRef<any>(null);
    const soundHappyRef = useRef<HTMLAudioElement | null>(null);
    const soundEmotionalRef = useRef<HTMLAudioElement | null>(null);
    const emotionTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const addToCart = CartStore((state) => state.addToCart);

    useEffect(() => {
        const el = messagesRef.current;
        if (el) el.scrollTop = el.scrollHeight;
    }, [messages]);

    useEffect(() => {
        const viewer = muffinRef.current;
        if (!viewer) return;

        function handleMouseMove(event: MouseEvent) {
            const x = event.clientX / window.innerWidth;
            const y = event.clientY / window.innerHeight;
            const baseAzimuth = 30;
            const basePolar = 100;
            const maxOffset = 35;

            const azimuth = baseAzimuth + (maxOffset - x * (maxOffset * 2));
            const polar = basePolar + (maxOffset - y * (maxOffset * 2));
            viewer.cameraOrbit = `${azimuth}deg ${polar}deg`;
        }

        window.addEventListener("mousemove", handleMouseMove as any);
        return () => window.removeEventListener("mousemove", handleMouseMove as any);
    }, []);

    useEffect(() => {
        if (pochiMood !== "idle") return;

        const interval = setInterval(() => {
            setIdleVariant((prev) => (prev === "float" ? "wiggle" : "float"));
        }, 6500);

        return () => clearInterval(interval);
    }, [pochiMood]);

    useEffect(() => {
        return () => {
            if (emotionTimeoutRef.current) clearTimeout(emotionTimeoutRef.current);
        };
    }, []);

    const triggerPochiMood = (nextMood: Exclude<PochiMood, "idle">, playSound = true) => {
        setPochiMood(nextMood);

        if (playSound) {
            const audioRef =
                nextMood === "happy" ? soundHappyRef.current : soundEmotionalRef.current;
            if (audioRef) {
                audioRef.currentTime = 0;
                void audioRef.play().catch(() => undefined);
            }
        }

        if (emotionTimeoutRef.current) clearTimeout(emotionTimeoutRef.current);
        emotionTimeoutRef.current = setTimeout(() => setPochiMood("idle"), 4000);
    };

    const handleSend = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const text = input.trim();
        if (!text || isSending) return;

        const userMessage: ChatMessage = {
            id: `${Date.now()}-user`,
            role: "user",
            content: text,
        };

        setMessages((prev) => [...prev, userMessage]);
        setInput("");
        setIsSending(true);

        const res = await RequestIAConsulta(text, normalizedMode);
        let answer = "Lo siento, no pude procesar tu consulta en este momento.";

        if (res.ok) {
            const data = res.data;
            if (typeof data === "string") answer = data;
            else if (data) answer = data.respuesta || data.answer || data.message || answer;
        } else if (normalizedMode === "Support") {
            answer =
                res.message ??
                "Nuestro canal de soporte está en construcción. Pronto podremos ayudarte desde aquí.";
        }

        const { cleanText, products } = extractProductsFromAnswer(answer);
        const assistantMessage: ChatMessage = {
            id: `${Date.now()}-assistant`,
            role: "assistant",
            content: cleanText,
            products,
        };

        setMessages((prev) => [...prev, assistantMessage]);
        setIsSending(false);
        triggerPochiMood("emotional", true);
    };

    const handleAddProductToCart = (product: IAProduct) => {
        const rawPrice = product.precioventa;
        const price =
            typeof rawPrice === "number"
                ? rawPrice
                : Number.parseFloat(rawPrice.replace(/[^\d.,]/g, "").replace(",", "."));

        const imageUrl = getProductImage(product);

        showToast({
            title: "Pochi, ¡Esta Contento!",
            message: "¡Pochi agradece que hayas elegido su recomendación!",
            type: "success",
        });
        addToCart({
            imageUrl,
            precioventa: Number.isFinite(price) ? price : 0,
            nombre: product.nombre,
            codproducto: product.codproducto,
            idProducto: product.idproducto,
        });

        triggerPochiMood(Math.random() > 0.5 ? "happy" : "emotional", true);
    };

    const handleMouseEnter = () => {
        if (Math.random() > 0.5) triggerPochiMood("happy", false);
    };

    const handleMouseLeave = () => {
        if (pochiMood === "idle") {
            setIdleVariant((prev) => (prev === "float" ? "wiggle" : "float"));
        }
    };

    const popover = (
        <Popover className="agentia__popover">
            <Popover.Header as="h3">Tu Querido Asistente POCHI</Popover.Header>
            <Popover.Body>
                <div className="agentia__chat">
                    <div className="agentia__messages" ref={messagesRef}>
                        {messages.map((msg) => (
                            <div
                                key={msg.id}
                                className={`agentia__bubble agentia__bubble--${msg.role}`}
                            >
                                {msg.content && <p>{renderFormattedText(msg.content)}</p>}

                                {msg.role === "assistant" &&
                                    msg.products &&
                                    msg.products.length > 0 && (
                                        <div className="agentia__products">
                                            {msg.products.map((product, index) => (
                                                <div
                                                    key={`${product.codproducto}-${index}`}
                                                    className="agentia__product-card"
                                                >
                                                    <img
                                                        src={getProductImage(product)}
                                                        alt={product.nombre}
                                                        className="agentia__product-image"
                                                    />
                                                    <div className="agentia__product-info">
                                                        <p className="agentia__product-name">
                                                            {product.nombre}
                                                        </p>
                                                        <p className="agentia__product-price">
                                                            {typeof product.precioventa === "number"
                                                                ? `S/ ${product.precioventa.toFixed(
                                                                      2
                                                                  )}`
                                                                : product.precioventa}
                                                        </p>
                                                        <button
                                                            type="button"
                                                            className="agentia__product-add"
                                                            onClick={() =>
                                                                handleAddProductToCart(product)
                                                            }
                                                        >
                                                            Agregar al carrito
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                            </div>
                        ))}
                    </div>

                    <form className="agentia__form" onSubmit={handleSend}>
                        <input
                            className="agentia__input"
                            placeholder="Escribe tu mensaje..."
                            value={input}
                            onChange={(event) => setInput(event.target.value)}
                        />

                        <button className="agentia__send" type="submit" disabled={isSending}>
                            {isSending ? "Enviando..." : "Enviar"}
                            <i className="fa-solid fa-paper-plane"></i>
                        </button>
                    </form>
                </div>
            </Popover.Body>
        </Popover>
    );

    const modelClassName = [
        "agentia__model",
        `agentia__model--${pochiMood}`,
        pochiMood === "idle" ? `agentia__model--${idleVariant}` : null,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div className="agentia" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
            <audio className="d-none" src="/emotional-pochi.mp3" ref={soundEmotionalRef} />
            <audio className="d-none" src="/happy-pochi.mp3" ref={soundHappyRef} />
            <OverlayTrigger trigger="click" placement="top" overlay={popover} rootClose>
                <button
                    onClick={() => triggerPochiMood("happy", true)}
                    className="agentia__launcher"
                    aria-label="Abrir asistente virtual"
                >
                    <div
                        className={modelClassName}
                        onClick={() => {
                            const wrapper = muffinRef.current?.parentElement;
                            if (!wrapper) return;
                            wrapper.classList.add("pop");
                            setTimeout(() => wrapper.classList.remove("pop"), 180);
                        }}
                    >
                        <model-viewer
                            ref={muffinRef}
                            src="assets/Muffin.glb"
                            shadow-intensity="0"
                        ></model-viewer>
                    </div>
                </button>
            </OverlayTrigger>
        </div>
    );
}

export default AgentIA;
