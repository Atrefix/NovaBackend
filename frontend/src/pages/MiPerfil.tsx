import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router";
import { TabItem, TabNavbar, TabPanel, TabsContent, TabsCustom } from "../components/TabsCustom";
import useAuthStore from "../stores/AuthStore";
import ViewInfoUser from "../components/ViewInfoUser";
import Layout from "./Layout";
import "./MiPerfil.css";
import { RequestPurchases } from "../api/PurchasesApi";
import { RequestProducts } from "../api/ProductApi";
import { RequestUpdateProfile } from "../api/AccountApi";
import { useQuery } from "@tanstack/react-query";
import ToastStore from "../stores/ToastStore";

const createEmptyProfileData = (): UpdateProfilePayload => ({
    alias: "",
    direccion: "",
    pais: "",
    fechaNacimiento: "",
});

const formatDateForInput = (value?: string | null) => {
    if (!value) return "";
    const raw = value.split("T")[0] ?? value;
    if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) return raw;
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return raw;
    return parsed.toISOString().split("T")[0];
};

function MiPerfil() {
    const navigate = useNavigate();
    const [editable, setEditable] = useState(false);

    const showToast = ToastStore((state) => state.showToast);
    const LogoutStore = useAuthStore((state) => state.logout);
    const userStore = useAuthStore((state) => state.user);
    const updateUserData = useAuthStore((state) => state.updateUserData);

    const { data: products } = useQuery({
        queryKey: ["products"],
        queryFn: () => RequestProducts().then((res) => res.data),
    });

    const [purchases, setPurchases] = useState<Purchase[]>([]);
    const [isLoadingPurchases, setIsLoadingPurchases] = useState(false);
    const [profileData, setProfileData] = useState<UpdateProfilePayload>(() =>
        createEmptyProfileData()
    );
    const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
    const [cancelingOrders, setCancelingOrders] = useState<Record<string, boolean>>({});
    const cancelTimeoutsRef = useRef<Map<string, ReturnType<typeof setTimeout>>>(new Map());

    useEffect(() => {
        if (!userStore) return;

        const load = async () => {
            setIsLoadingPurchases(true);
            const res = await RequestPurchases();
            if (res.ok && res.data) setPurchases(res.data as Purchase[]);
            setIsLoadingPurchases(false);
        };

        void load();
    }, [userStore]);

    useEffect(() => {
        return () => {
            cancelTimeoutsRef.current.forEach((timeoutId) => {
                clearTimeout(timeoutId);
            });
            cancelTimeoutsRef.current.clear();
        };
    }, []);

    useEffect(() => {
        if (!userStore) {
            setProfileData(createEmptyProfileData());
            return;
        }

        setProfileData({
            alias: userStore.alias ?? "",
            direccion: userStore.direccion ?? "",
            pais: userStore.pais ?? "",
            telefono: userStore.telefono ?? "",
            fechaNacimiento: formatDateForInput(userStore.fechaNacimiento),
        });
    }, [userStore]);

    const handleLogout = async () => {
        await LogoutStore();
        navigate("/login");
    };

    const handleProfileButtonClick = () => {
        if (!editable) {
            setEditable(true);
            return;
        }

        void handleSaveProfile();
    };

    const handleProfileFieldChange = (field: keyof UpdateProfilePayload) => (newValue: string) => {
        setProfileData((prev) => ({ ...prev, [field]: newValue }));
    };

    const buildPurchaseKey = (purchase: Purchase, itemsCountParam?: number) => {
        if (purchase.id !== undefined && purchase.id !== null) {
            return String(purchase.id);
        }
        const itemsCount =
            typeof itemsCountParam === "number"
                ? itemsCountParam
                : purchase.detalles.reduce((acc, item) => acc + item.quantity, 0);
        return `${purchase.total}-${itemsCount}`;
    };

    const handleCancelPurchase = (purchaseKey: string) => {
        if (cancelingOrders[purchaseKey]) return;

        setCancelingOrders((prev) => ({ ...prev, [purchaseKey]: true }));

        const timeoutId = setTimeout(() => {
            setPurchases((prev) =>
                prev.filter((purchase) => buildPurchaseKey(purchase) !== purchaseKey)
            );
            setCancelingOrders((prev) => {
                const next = { ...prev };
                delete next[purchaseKey];
                return next;
            });
            cancelTimeoutsRef.current.delete(purchaseKey);
        }, 1500);

        cancelTimeoutsRef.current.set(purchaseKey, timeoutId);
    };

    const handleSaveProfile = async () => {
        if (isUpdatingProfile) return;

        const payload: UpdateProfilePayload = {
            alias: profileData.alias.trim(),
            direccion: profileData.direccion.trim(),
            pais: profileData.pais.trim(),
            telefono: profileData.telefono.trim(),
            fechaNacimiento: profileData.fechaNacimiento,
        };

        if (!payload.alias) {
            showToast({
                title: "Error",
                message: "Por favor ingresa tu nombre y apellido.",
                type: "error",
            });
            return;
        }
        if (!payload.direccion) {
            showToast({
                title: "Error",
                message: "Por favor ingresa una dirección.",
                type: "error",
            });
            return;
        }
        if (!payload.pais) {
            showToast({
                title: "Error",
                message: "Por favor ingresa tu país.",
                type: "error",
            });
            return;
        }
        if (!payload.fechaNacimiento) {
            showToast({
                title: "Error",
                message: "Por favor selecciona tu fecha de nacimiento.",
                type: "error",
            });
            return;
        }

        setIsUpdatingProfile(true);
        const res = await RequestUpdateProfile(payload);
        setIsUpdatingProfile(false);

        if (!res.ok) {
            showToast({
                title: "Error",
                message: res.message || "No se pudo actualizar el perfil. Intenta nuevamente.",
                type: "error",
            });
            return;
        }

        updateUserData(payload);
        setEditable(false);
        showToast({
            title: "Éxito",
            message: "Perfil actualizado correctamente.",
            type: "success",
        });
    };

    return (
        <Layout className="wrapper">
            <div className="wrapper__container">
                <h1 className="wrapper__title">Mi Perfil</h1>

                <TabsCustom defaultActiveKey="datos">
                    <TabNavbar>
                        <TabItem eventKey="datos" title="Datos" />
                        <TabItem eventKey="pedidos" title="Pedidos" />
                    </TabNavbar>
                    <TabsContent>
                        <TabPanel
                            eventKey="datos"
                            className="d-flex flex-column justify-content-center gap-2"
                        >
                            <ViewInfoUser title="Correo Electrónico" value={userStore?.email} />
                            <ViewInfoUser
                                title="Nombre y Apellido"
                                value={profileData.alias}
                                editable={editable}
                                onChange={handleProfileFieldChange("alias")}
                            />
                            <ViewInfoUser
                                title="Dirección"
                                value={profileData.direccion}
                                editable={editable}
                                onChange={handleProfileFieldChange("direccion")}
                            />
                            <ViewInfoUser
                                title="Fecha de Nacimiento"
                                value={
                                    editable
                                        ? profileData.fechaNacimiento
                                        : new Date(profileData.fechaNacimiento).toLocaleDateString(
                                              "es-ES",
                                              {
                                                  day: "2-digit",
                                                  month: "2-digit",
                                                  year: "numeric",
                                              }
                                          )
                                }
                                editable={editable}
                                onChange={handleProfileFieldChange("fechaNacimiento")}
                                inputType="date"
                            />
                            <ViewInfoUser
                                title="Teléfono"
                                value={profileData.telefono}
                                editable={editable}
                                onChange={(value) => {
                                    if (/^\d*$/.test(value)) {
                                        handleProfileFieldChange("telefono")(value);
                                    }
                                }}
                            />
                            <ViewInfoUser
                                title="País"
                                value={profileData.pais}
                                editable={editable}
                                onChange={handleProfileFieldChange("pais")}
                            />
                            <div className="perfil__content__data__actions">
                                {userStore?.rol === "ADMIN" && (
                                    <Link to="/dashboard" className="btn-filled">
                                        Ir Panel de Control
                                    </Link>
                                )}
                                <button
                                    type="button"
                                    className="btn-filled"
                                    onClick={handleProfileButtonClick}
                                    disabled={editable && isUpdatingProfile}
                                >
                                    {editable
                                        ? isUpdatingProfile
                                            ? "Guardando..."
                                            : "Guardar"
                                        : "Editar Datos"}
                                </button>
                                <button className="btn-filled" type="button" onClick={handleLogout}>
                                    Cerrar Sesion
                                </button>
                            </div>
                        </TabPanel>

                        <TabPanel eventKey="pedidos">
                            {isLoadingPurchases && (
                                <p className="pedidos__text">Cargando historial de compras...</p>
                            )}

                            {!isLoadingPurchases && purchases.length === 0 && (
                                <p className="pedidos__text">No tienes compras registradas aún.</p>
                            )}

                            {!isLoadingPurchases && purchases.length > 0 && (
                                <div className="perfil__orders__list">
                                    {purchases.map((purchase) => {
                                        const itemsCount = purchase.detalles.reduce(
                                            (acc, item) => acc + item.quantity,
                                            0
                                        );

                                        const totalFormatted = Number(purchase.total).toFixed(2);
                                        const purchaseKey = buildPurchaseKey(purchase, itemsCount);

                                        return (
                                            <article
                                                key={purchaseKey}
                                                className="perfil__orders__item"
                                            >
                                                <div className="perfil__orders__header">
                                                    <div>
                                                        <p className="perfil__orders__total">
                                                            Total: S/ {totalFormatted}
                                                        </p>
                                                        <p className="perfil__orders__meta">
                                                            {itemsCount} productos
                                                        </p>
                                                    </div>
                                                    <p className="perfil__orders__address">
                                                        Entrega en {purchase.addressDelivery}
                                                        {purchase.cityDelivery
                                                            ? `, ${purchase.cityDelivery}`
                                                            : ""}
                                                    </p>
                                                </div>

                                                {purchase.detalles.map((item) => (
                                                    <div
                                                        key={item.productId}
                                                        className="payment__summary__item"
                                                    >
                                                        <div>
                                                            <img
                                                                className="payment__summary__image"
                                                                src={
                                                                    products?.find(
                                                                        (x) =>
                                                                            x.id === item.productId
                                                                    )?.imageUrl
                                                                }
                                                                alt={item.productName}
                                                            />
                                                            <div>
                                                                <p className="payment__summary__title">
                                                                    {item.productName}
                                                                </p>
                                                                <span className="payment__summary__meta">
                                                                    x
                                                                    {item.quantity
                                                                        .toString()
                                                                        .padStart(2, "0")}
                                                                </span>
                                                            </div>
                                                        </div>
                                                        <p className="payment__summary__price">
                                                            S/{" "}
                                                            {(item.price * item.quantity).toFixed(
                                                                2
                                                            )}
                                                        </p>
                                                    </div>
                                                ))}
                                                <div className="perfil__orders__actions">
                                                    <button
                                                        type="button"
                                                        className="perfil__orders__cancel"
                                                        onClick={() =>
                                                            handleCancelPurchase(purchaseKey)
                                                        }
                                                        disabled={
                                                            cancelingOrders[purchaseKey] === true
                                                        }
                                                    >
                                                        {cancelingOrders[purchaseKey]
                                                            ? "Cancelando..."
                                                            : "Cancelar Pedido"}
                                                    </button>
                                                </div>
                                            </article>
                                        );
                                    })}
                                </div>
                            )}
                        </TabPanel>
                    </TabsContent>
                </TabsCustom>
            </div>
        </Layout>
    );
}

export default MiPerfil;
