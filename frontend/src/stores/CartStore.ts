import { create } from "zustand";
import { persist } from "zustand/middleware";

const buildCustomKey = (codproducto: string, customizations?: CartItemCustomization[]) => {
    if (!customizations || customizations.length === 0) return codproducto;

    const serialized = [...customizations]
        .sort((a, b) => a.id.localeCompare(b.id))
        .map((option) => `${option.id}:${option.value}`)
        .join("|");

    return `${codproducto}__${serialized}`;
};

const matchesItem = (item: CartItem, codproducto: string, customKey?: string) => {
    const key = item.customKey ?? buildCustomKey(item.codproducto, item.customizations);
    return customKey ? key === customKey : item.codproducto === codproducto;
};

const CartStore = create<CartStore>()(
    persist(
        (set, get) => ({
            cart: [],

            addToCart: (item) => {
                const { cart } = get();

                console.log(item);

                const normalizedCustomizations =
                    item.customizations && item.customizations.length > 0
                        ? item.customizations
                        : undefined;

                const customKey =
                    item.customKey ?? buildCustomKey(item.codproducto, normalizedCustomizations);

                const existingIndex = cart.findIndex(
                    (p) =>
                        (p.customKey ?? buildCustomKey(p.codproducto, p.customizations)) ===
                        customKey
                );

                console.log(existingIndex, customKey, normalizedCustomizations);

                if (existingIndex >= 0) {
                    set({
                        cart: cart.map((p, idx) =>
                            idx === existingIndex ? { ...p, quantity: p.quantity + 1 } : p
                        ),
                    });
                } else {
                    set({
                        cart: [
                            ...cart,
                            {
                                ...item,
                                customizations: normalizedCustomizations,
                                customKey,
                                quantity: item.quantity ?? 1,
                            },
                        ],
                    });
                }
            },

            decreaseQuantity: (codproducto, customKey) => {
                const { cart } = get();
                set({
                    cart: cart
                        .map((p) =>
                            matchesItem(p, codproducto, customKey)
                                ? { ...p, quantity: p.quantity - 1 }
                                : p
                        )
                        .filter((p) => p.quantity > 0),
                });
            },

            removeFromCart: (codproducto, customKey) => {
                set({
                    cart: get().cart.filter((p) => !matchesItem(p, codproducto, customKey)),
                });
            },

            clearCart: () => set({ cart: [] }),

            total: () => {
                return get().cart.reduce((acc, p) => acc + p.precioventa * p.quantity, 0);
            },
        }),
        {
            name: "cart-storage",
        }
    )
);

export default CartStore;
