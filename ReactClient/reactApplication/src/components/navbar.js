import React from "react";
import "../styles/navbar.css";

const Navbar = ({ setRenderState, size, isLoggedIn, handleLogout }) => {
  return (
    <nav>
      <div className="nav_box">
        <span className="my_shop" onClick={() => setRenderState("store")}>
          Periodic deals
        </span>
        {isLoggedIn ? (
        <span className="my_shop" onClick={handleLogout}>
          Logout
        </span>
        ) : (
        <span className="my_shop" onClick={() => setRenderState("login")}>
          Login
        </span>
        )}

        {isLoggedIn ? (
        <span className="my_shop_hidden" >
          Register
        </span>
        ) : (
        <span className="my_shop" onClick={() => setRenderState("register")}>
          Register
        </span>
        )}
        <div className="cart" onClick={() => setRenderState("cart")}>
          <span>
            <i className="fas fa-cart-plus"></i>
          </span>
          <span>{size}</span>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
