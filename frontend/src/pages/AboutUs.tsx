import Layout from "./Layout";
import "./AboutUs.css";

function AboutUs() {
    return (
        <Layout className="wrapper" agentMode="Support">
            <div className="wrapper__container">
                <h2 id="mission" className="wrapper__title">
                    Sobre Nosotros
                </h2>

                <article className="aboutus">
                    <section className="aboutus__card">
                        <div className="aboutus__card__header">
                            <h4>Nuestra Misión</h4>
                            <p>
                                Lorem ipsum dolor, sit amet consectetur adipisicing elit. Suscipit
                                dolores nihil ducimus temporibus animi consectetur labore excepturi
                                officiis, adipisci soluta quia nam repellendus voluptatem nostrum,
                                harum dolore dolorem consequuntur vitae!
                            </p>
                        </div>
                        <div className="aboutus__card__image">
                            <img src="/assets/10.png" alt="mission le pettite coffee" />
                        </div>
                    </section>
                    <section className="aboutus__card">
                        <div className="aboutus__card__image">
                            <img src="/assets/11.png" alt="mission le pettite coffee" />
                        </div>
                        <div className="aboutus__card__header">
                            <h4>Nuestra Visión</h4>
                            <p>
                                Lorem ipsum dolor, sit amet consectetur adipisicing elit. Suscipit
                                dolores nihil ducimus temporibus animi consectetur labore excepturi
                                officiis, adipisci soluta quia nam repellendus voluptatem nostrum,
                                harum dolore dolorem consequuntur vitae!
                            </p>
                        </div>
                    </section>
                </article>

                <h2 id="contact" className="wrapper__title">
                    Contáctanos
                </h2>

                <article className="contactus">
                    <form className="reviews__form" action="" noValidate>
                        <section className="form__avatar">
                            <span></span>
                        </section>
                        <section className="form__content">
                            <div className="form__content-section">
                                <div className="input">
                                    <label htmlFor="names">Nombre y Apellido:</label>
                                    <input type="text" placeholder="ej. Juan Pablo" id="names" />
                                </div>

                                <div className="input">
                                    <label htmlFor="email">Correo Electrónico:</label>
                                    <input
                                        type="email"
                                        placeholder="ej. juanpablo@gmail.com"
                                        id="email"
                                    />
                                </div>
                            </div>
                            <textarea placeholder="Escribe tu hermoso pedido..." id=""></textarea>
                            <div className="form__content-actions2">
                                <button>
                                    ENVIAR PETICIÓN
                                    <i className="fa-solid fa-chevron-right"></i>
                                </button>
                            </div>
                        </section>
                    </form>
                </article>
            </div>
        </Layout>
    );
}

export default AboutUs;
