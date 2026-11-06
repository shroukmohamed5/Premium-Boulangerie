# Premium — Luxury Bakery Landing Page

A single-file, responsive landing page for a high-end bakery. The hero cycles through four animated product scenes (Made to swirl → Layered with warmth → Baked to rise → Worth the swirl) with cinematic slide/swirl transitions. Built with plain HTML, CSS and vanilla JavaScript. No dependencies, no build step.

## Features

- Sticky, blurred header with brand, navigation (Pastries, Craft, Bakery, Story), account and bag icons
- Hero carousel with swirl/slide transitions, animated headlines, rotating badge and mouse parallax
- Autoplay (keeps running, restarts after manual navigation), arrow buttons, dots, keyboard arrows and swipe support
- Pastries, Craft, Bakery and Story sections with scroll-reveal animations
- Working "Add to bag" counter and toast notifications
- Fully responsive with a mobile menu
- Respects `prefers-reduced-motion`
- Real photography in `assets/` (WebP): hero carousel with slow Ken Burns zoom, product cards with 3D tilt, parallax craft gallery
- Page loader, scroll progress bar, floating flour particles, marquee ribbon, animated counters
- Bag drawer with quantities, delivery / pick-up switch, delivery fee (free over $25) and a running total; Delivery & Reservation section with date-aware time slots; live open/closed status, newsletter form

## Project structure

```
premium-landing/
├── index.html   # HTML + CSS + JS
├── assets/      # photos (WebP)
└── README.md
```

## Getting started

### Option 1: Open the file
Download or clone the repository and double-click `index.html`. It opens in any modern browser.

### Option 2: Local server
```bash
cd premium-landing
python -m http.server 8000
```
Then visit http://localhost:8000.

### Option 3: VS Code
Install the **Live Server** extension, right-click `index.html` and choose **Open with Live Server**.

> Fonts (Playfair Display and Jost) load from Google Fonts. Without an internet connection the page still works and falls back to Georgia and Arial.

## Deploy with GitHub Pages

1. Push the project to a GitHub repository.
2. Go to **Settings → Pages**.
3. Under **Build and deployment**, choose **Deploy from a branch**, select `main` and `/ (root)`, then save.
4. Your site will be live at `https://<your-username>.github.io/<repository-name>/` after a minute or two.

If `index.html` is inside a subfolder, select that folder as the source or move the file to the repository root.

## Customization

All changes are made in `index.html`.

| To change | Look for |
|---|---|
| Brand name | `Premium` in the header and footer |
| Hero headlines and subtext | the `slides` array at the top of the script |
| Colors | the CSS variables in `:root` (`--cream`, `--brown`, `--gold`, ...) |
| Products and prices | the `products` array |
| Opening hours (live status) | the `sched` array in the script |
| Autoplay speed | the `5600` value in the `restart()` function |

To swap a photo, replace the file in `assets/` with the same name, or edit the `src` in `index.html`.

## Browser support

Latest versions of Chrome, Edge, Firefox and Safari.

## License

Free to use and modify for personal and educational projects.

## v4 notes

- **WhatsApp**: set the `WA` constant at the top of the "Menu, coffee bar, bag, WhatsApp" block in `index.html` (digits only, with country code). Orders and reservations open a ready-made WhatsApp message.
- **Menu**: edit the `M` array (name, price, note, ingredients, allergens).
- **Photos**: every menu item loads `assets/<name>.webp` (or .jpg / .png). Replace a file with the same name to change a photo. If a file is missing, coffee items show an animated cup illustration.
- **Open the folder after extracting the zip**: opening `index.html` from inside the archive hides the images.
- **Promo code**: `WELCOME10` (10% off). Delivery fee and free-delivery threshold: `FEE` and `FREE`.
- All names, prices, address, phone and reviews are placeholders.

## v10 notes

- **Language**: the AR/EN button in the header switches the page and its direction (RTL). Translations live in the `AR` object (and `TOK` for ingredients) near the end of the script; unknown text stays in English.
- **Offers**: edit the `DEALS` array (prices, names, images).
- **Hero**: 8 slides, edit the `slides` array (English and `ar` text).
