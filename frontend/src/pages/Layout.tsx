import Header from "../components/Header";
import Footer from "../components/Footer";
import ScrollTopButton from "../components/ScrollTopButton";
import AgentIA from "../components/AgentIA";
import { type AgentIAMode } from "../api/IAApi";

type LayoutProps = {
    children?: React.ReactNode;
    className?: string;
    agentMode?: AgentIAMode | "none";
};

function Layout({ children, className, agentMode = "recommendations" }: LayoutProps) {
    return (
        <>
            <Header />
            <main className={`layout__main page-fade ${className ?? ""}`}>{children}</main>
            <Footer />
            <ScrollTopButton />
            {agentMode !== "none" && <AgentIA mode={agentMode} />}
        </>
    );
}

export default Layout;
