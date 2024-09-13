import React, { useState, useEffect } from "react";
import Store from "./components/store";
import Navbar from "./components/navbar";
import Cart from "./components/cart";
import Payment from "./components/payment";
import useItems from "./itemHook";
import Login from "./components/login";
import Register from "./components/register";

// Add this line at the top of your file, after the imports
const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:9000';

const App = () => {
  const [items] = useItems();
  const [renderState, setRenderState] = useState("store");
  const [cart, setCart] = useState([]);
  const [values, setValues] = useState({
      firstName: '',
      lastName: '',
      email: '',
  });
  // const [password, setPassword] = useState(null);
  const [submitted, setSubmitted] = useState(false);
  const [valid, setValid] = useState(false);
  const [user, setUser] = useState(null);
  const [authToken, setAuthToken] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const handleFirstNameInputChange = (event) => {
    event.persist();
    setValues((values) => ({
        ...values,
        firstName: event.target.value,
    }));
  };

  const handleLastNameInputChange = (event) => {
  	event.persist();
  	setValues((values) => ({
  		...values,
  		lastName: event.target.value,
  	}));
  };

  const handleEmailInputChange = (event) => {
  	event.persist();
  	setValues((values) => ({
  		...values,
  		email: event.target.value,
  	}));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    console.log(values);
    console.log(valid);
    if(values.firstName && values.lastName && values.email) {
      setValid(true);
      const data = values
      const requestOptions = {
        method: "POST",
        headers: { "Content-Type": "application/json", "key": "1" },
        body: JSON.stringify(data)
      };
      fetch(`${BACKEND_URL}/api/payment`, requestOptions)
    }
    setSubmitted(true);
  }

  const handleClick = (item) => {
    if (cart.indexOf(item) !== -1) return;
    setCart([...cart, item]);
  };

  const handleChange = (item, d) => {
    const ind = cart.indexOf(item);
    const arr = cart;
    arr[ind].qty += d;

    if (arr[ind].qty === 0) arr[ind].qty = 1;
    setCart([...arr]);
  };

  const HandleLogin = async (email, password) => {
    try {
      const response = await fetch(`${BACKEND_URL}/api/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({'email': email, 'password': password }),
      });
      const data = await response.json();
      if (response.ok) {
        // setUser(data.user);
        console.log(data);
        setAuthToken(data.token);
        setIsLoggedIn(true);
        setRenderState("store");
      } else {
        console.error("Login failed:", data.message);
      }
    } catch (error) {
      console.error("Login error:", error);
    }
  };

  useEffect(() => {
    if (isLoggedIn && authToken) {
      FetchCartData();
    }
  }, [isLoggedIn, authToken]);

  const HandleRegister = async (email, password, firstName, lastName) => {
    try {
      console.log("Registering user:", email, password, firstName, lastName);
      const response = await fetch(`${BACKEND_URL}/api/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({'email': email, 'password': password, 'firstName': firstName, 'lastName': lastName }),
      });
      console.log("Za stringifajem");
      const data = await response.json();
      console.log("Za stringifajem");
      console.log(data);
      if (response.ok) {
        setUser(data.user);
        setAuthToken(data.token);
        setRenderState("store");
      } else {
        console.error("Registration failed:", data.message);
      }
    } catch (error) {
      console.error("Registration error:", error);
    }
  };

  const SendCartData = async () => {
    if (!isLoggedIn) {
      return;
    }
    try {
      const cartString = JSON.stringify(cart);
      const response = await fetch(`${BACKEND_URL}/api/update_cart`, {
        method: "POST",
        headers: { 
          "Content-Type": "application/json", 
          "key": "1",
          "Authorization": `Bearer ${authToken}` 
        },
        body: JSON.stringify({ cart: cartString }),
      });
      const data = await response.json();
      console.log(data);
      if (response.ok) {
        console.log("Cart data sent successfully");
      } else {
        console.error("Failed to send cart data");
      }
    } catch (error) {
      console.error("Error sending cart data:", error);
    }
  };

  const FetchCartData = async () => {
    console.log(isLoggedIn);
    console.log(authToken);
    if (!isLoggedIn || !authToken) {
      console.log("User not logged in or missing auth token");
      return;
    }
  
    try {
      const response = await fetch(`${BACKEND_URL}/api/get_cart`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${authToken}`,
          "Content-Type": "application/json"
        }
      });
  
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
  
      const data = await response.json();
      
      if (data.cart) {
        const parsedCart = JSON.parse(data.cart);
        setCart(parsedCart);
        console.log("Cart data fetched successfully", parsedCart);
      } else {
        console.log("No cart data found");
        // setCart([]);
      }
    } catch (error) {
      console.error("Error fetching cart data:", error);
    }
  };  

  const HandleLogout = () => {
    SendCartData();
    setUser(null);
    setAuthToken(null);
    setCart([]);
    setIsLoggedIn(false);
    setRenderState("store");
  };

  // const handleGoogleLogin = () => {
  //   window.location.href = "http://localhost:9000/login/google";
  // };

  // useEffect(() => {
  //   // Check for token in URL after Google OAuth redirect
  //   const urlParams = new URLSearchParams(window.location.search);
  //   const token = urlParams.get('token');
  //   if (token) {
  //     setAuthToken(token);
  //     setIsLoggedIn(true);
  //     // Remove token from URL
  //     window.history.replaceState({}, document.title, "/");
  //     // Fetch cart data or perform other post-login actions
  //     fetchCartData();
  //   }
  // }, []);

  // const fetchCartData = async () => {
  //   if (!isLoggedIn || !authToken) return;

  //   try {
  //     const response = await fetch("http://localhost:9000/api/get_cart", {
  //       headers: {
  //         "Authorization": `Bearer ${authToken}`
  //       }
  //     });
  //     if (response.ok) {
  //       const data = await response.json();
  //       setCart(JSON.parse(data.cart));
  //     } else {
  //       console.error("Failed to fetch cart data");
  //     }
  //   } catch (error) {
  //     console.error("Error fetching cart data:", error);
  //   }
  // };

  return (
    <React.Fragment>
      <Navbar setRenderState={setRenderState} size={cart.length} user={user} onLogout={HandleLogout} isLoggedIn={isLoggedIn} handleLogout={HandleLogout} />
      {renderState === "store" && <Store handleClick={handleClick} items={items} />}
      {renderState === "cart" && <Cart cart={cart} setCart={setCart} handleChange={handleChange} setRenderState={setRenderState} />}
      {renderState === "payment" && <Payment values={values} handleFirstNameInputChange={handleFirstNameInputChange} handleLastNameInputChange={handleLastNameInputChange} handleEmailInputChange={handleEmailInputChange} submitted={submitted} handleSubmit={handleSubmit} valid={valid}/> }
      {renderState === "login" && <Login onLogin={HandleLogin} />}
      {renderState === "register" && <Register onRegister={HandleRegister} />}
    </React.Fragment>
  );
};

export default App;
