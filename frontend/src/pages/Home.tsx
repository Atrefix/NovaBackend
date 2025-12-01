import { Link } from "react-router";
import { useEffect, useRef } from "react";
import Layout from "./Layout";
import "./Home.css";

function Home() {
    const bgRef = useRef<HTMLImageElement | null>(null);

    useEffect(() => {
        const handleScroll = () => {
            const y = window.scrollY;
            if (bgRef.current) {
                bgRef.current.style.transform = `translateY(${y * 0.2}px)`;
            }
        };

        handleScroll();
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    useEffect(() => {
        const elements = document.querySelectorAll<HTMLElement>(".details");
        if (!elements.length) return;

        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    const el = entry.target as HTMLElement;
                    if (entry.isIntersecting) {
                        el.classList.add("details--visible");
                        el.classList.remove("details--hidden");
                    } else {
                        el.classList.remove("details--visible");
                        el.classList.add("details--hidden");
                    }
                });
            },
            { threshold: 0.2 }
        );

        elements.forEach((el) => {
            el.classList.add("details--hidden");
            observer.observe(el);
        });

        return () => observer.disconnect();
    }, []);

    const sponsors = [
        "ORGANIC BEANS",
        "PREMIUM ROAST",
        "SWEET BAKERY",
        "LOCAL FARMERS",
        "FAIR TRADE",
        "FRESH MILK",
        "CHOCOLATE LOVERS",
        "BEST COFFEE 2024",
    ];

    return (
        <Layout>
            <img
                ref={bgRef}
                className="background-home"
                src="https://plus.unsplash.com/premium_photo-1668472273029-ba03dfaf5c45?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                alt="background le pettite coffee"
            />

            <article className="hero">
                <div className="hero__container">
                    <h3 className="hero__caption">Delicia en tu boca</h3>
                    <h2 className="hero__title">
                        Caprichos dulces,
                        <br /> platos perfectos
                    </h2>
                    <div className="hero__links">
                        <Link to="/menus">ESTOY PREPARADO</Link>
                    </div>
                </div>
                <div className="sponsors-strip">
                    <div className="sponsors-track">
                        {[...sponsors, ...sponsors, ...sponsors].map((text, i) => (
                            <span key={i} className="sponsor-item">
                                <i className="fa fa-star"></i> {text}
                            </span>
                        ))}
                    </div>
                </div>
            </article>

            <article className="discounts">
                <div className="discounts__container">
                    <section className="discounts__display">
                        <h3 className="discounts__title">20% Descuento en tu primera compra</h3>
                        <p className="discounts__description">
                            Disfruta tortas, muffins, panes y café recién hecho al mejor precio.
                        </p>
                        <Link to="/menus">Ver Mas</Link>
                    </section>
                    <section className="discounts__images">
                        <img src="/assets/2_border.png" alt="Discounts" />
                        <img src="/assets/3.webp" alt="Discounts 2" />
                        <img src="/assets/4_border.jpg" alt="Discounts 3" />
                    </section>
                </div>
            </article>

            <article id="faq" className="faq">
                <div className="faq__container">
                    <section className="faq__display">
                        <h3 className="faq__title">Preguntas Frecuentes</h3>
                        <p className="faq__description">
                            ¿Tienes dudas sobre nuestros pedidos, envíos o productos? Aquí
                            encontrarás las respuestas más comunes para ayudarte a disfrutar de tu
                            experiencia dulce sin complicaciones.
                        </p>
                    </section>
                    <section className="faq__content">
                        <details className="details">
                            <summary className="details__summary">
                                ¿Qué es un café de Le Petite?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Un café de Le Petite es una bebida preparada con granos
                                seleccionados y sabores únicos como leche, chocolate, cacao y
                                especias locales. Nuestra propuesta es combinar tradición con un
                                toque artesanal.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Hacen tortas personalizadas para cumpleaños o eventos?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Sí, elaboramos tortas a pedido con diseños personalizados. Puedes
                                elegir sabores, rellenos y decoración. Te recomendamos encargarla
                                con al menos 48 horas de anticipación.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Ofrecen opciones sin azúcar o para personas con diabetes?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Claro, contamos con tortas, muffins y panes endulzados con stevia o
                                miel, ideales para quienes buscan opciones más saludables.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Tienen productos sin gluten o para celíacos?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Sí, disponemos de muffins, panes y algunos pasteles sin gluten. Sin
                                embargo, se preparan en la misma cocina, por lo que puede existir
                                contaminación cruzada.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Se pueden hacer pedidos en línea o por teléfono?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Sí, puedes hacer tu pedido desde nuestra página web o llamarnos
                                directamente. También aceptamos reservas por WhatsApp para mayor
                                comodidad.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Hacen entregas a domicilio?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Ofrecemos delivery dentro de la ciudad con un costo adicional. El
                                tiempo de entrega depende de la ubicación y disponibilidad.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Qué métodos de pago aceptan?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Aceptamos efectivo, tarjetas de débito, crédito y pagos electrónicos
                                como transferencias o billeteras digitales.
                            </div>
                        </details>

                        <details className="details">
                            <summary className="details__summary">
                                ¿Ofrecen combos o promociones especiales?
                                <i className="fa fa-chevron-down"></i>
                            </summary>
                            <div className="details__content">
                                Sí, cada semana tenemos promociones en café con muffins, y
                                descuentos en tortas por temporada. Consulta nuestras redes sociales
                                para conocer las ofertas vigentes.
                            </div>
                        </details>
                    </section>
                </div>
            </article>
        </Layout>
    );
}

export default Home;
