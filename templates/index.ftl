<!DOCTYPE html>
<html>
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <title>Parlament Browser</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.2.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">
    <link href="css/sb-admin-2.min.css" rel="stylesheet">
    <link href="css/site.css" rel="stylesheet">

    <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.slim.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.4.1/jquery.easing.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="js/sb-admin-2.min.js"></script>
    <script src="https://d3js.org/d3.v6.js"></script>
    <script src="js/RadarChart.js"></script>
    <script src="js/charts.js"></script>
    <script src="js/panel.js"></script>
    <script src="js/corpus.js"></script>
    <script src="js/login.js"></script>
    <script src="js/index.js"></script>
</head>
<body id="page-top">
    <div id="wrapper">
        <ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
            <!-- Nav Item - Dashboard -->
            <li class="nav-item active">
                <a class="nav-link" href="/">
                    <i class="fa-solid fa-landmark"></i>
                    <span>Parlament Browser</span></a>
            </li>

            <!-- Divider -->
            <hr class="sidebar-divider">

            <!-- Heading -->
            <div class="sidebar-heading">
                Suche
            </div>

            <!-- Nav Item - Utilities Collapse Menu -->
            <li class="nav-item">
                <a class="nav-link" type="button" onclick="switchCtx('corpus-index')">
                    <i class="fa-solid fa-book"></i>
                    <span>Korpus Index</span>
                </a>
            </li>

            <!-- Nav Item - Pages Collapse Menu -->
            <li class="nav-item">
                <a class="nav-link" type="button" onclick="switchCtx('visualization')">
                    <i class="fa-solid fa-chart-simple"></i>
                    <span>Visualisierung</span>
                </a>
            </li>

            <!-- Nav Item - Pages Collapse Menu -->
            <#if loggedIn>
                <li class="nav-item">
                    <a class="nav-link" type="button" onclick="switchCtx('messaging')">
                        <i class="fa-solid fa-envelope"></i>
                        <span>Nachrichten</span>
                    </a>
                </li>
            </#if>

            <#if loggedIn && (admin || protocol || speech || template)>
                <!-- Divider -->
                <hr class="sidebar-divider">

                <!-- Heading -->
                <div class="sidebar-heading">
                    Administration
                </div>

                <#if admin>
                    <!-- Nav Item - Utilities Collapse Menu -->
                    <li class="nav-item">
                        <a class="nav-link" href="/admin">
                            <i class="fa-solid fa-user"></i>
                            <span>Admin Panel</span>
                        </a>
                    </li>
                </#if>

                <#if protocol || speech || template>
                    <!-- Nav Item - Utilities Collapse Menu -->
                    <li class="nav-item">
                        <a class="nav-link" href="/edit">
                            <i class="fa-solid fa-book-open"></i>
                            <span>Editierungsumgebung</span>
                        </a>
                    </li>
                </#if>
            </#if>

        </ul>
        <div id="content-wrapper-vis" class="d-flex flex-column w-100">
            <div id="content">
                <!-- Topbar -->
                <nav class="navbar navbar-expand navbar-light bg-white topbar mb-4 static-top shadow">

                    <!-- Sidebar Toggle (Topbar) -->
                    <button id="sidebarToggleTop" class="btn btn-link d-md-none rounded-circle mr-3">
                        <i class="fa fa-bars"></i>
                    </button>

                    <!-- Topbar Navbar -->
                    <ul class="navbar-nav ml-auto">

                        <!-- Nav Item - Search Dropdown (Visible Only XS) -->
                        <li class="nav-item dropdown no-arrow d-sm-none">
                            <a class="nav-link dropdown-toggle" href="#" id="searchDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-search fa-fw"></i>
                            </a>
                            <!-- Dropdown - Messages -->
                            <div class="dropdown-menu dropdown-menu-right p-3 shadow animated--grow-in"
                                aria-labelledby="searchDropdown">
                                <form class="form-inline mr-auto w-100 navbar-search">
                                    <div class="input-group">
                                        <input type="text" class="form-control bg-light border-0 small"
                                            placeholder="Search for..." aria-label="Search"
                                            aria-describedby="basic-addon2">
                                        <div class="input-group-append">
                                            <button class="btn btn-primary" type="button">
                                                <i class="fas fa-search fa-sm"></i>
                                            </button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </li>

                        <!-- Nav Item - Alerts -->
                        <li class="nav-item dropdown no-arrow mx-1">
                            <a class="nav-link dropdown-toggle" href="#" id="alertsDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-bell fa-fw"></i>
                                <!-- Counter - Alerts -->
                                <span class="badge badge-danger badge-counter">3+</span>
                            </a>
                            <!-- Dropdown - Alerts -->
                            <div class="dropdown-list dropdown-menu dropdown-menu-right shadow animated--grow-in"
                                aria-labelledby="alertsDropdown">
                                <h6 class="dropdown-header">
                                    Alerts Center
                                </h6>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="mr-3">
                                        <div class="icon-circle bg-primary">
                                            <i class="fas fa-file-alt text-white"></i>
                                        </div>
                                    </div>
                                    <div>
                                        <div class="small text-gray-500">December 12, 2019</div>
                                        <span class="font-weight-bold">A new monthly report is ready to download!</span>
                                    </div>
                                </a>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="mr-3">
                                        <div class="icon-circle bg-success">
                                            <i class="fas fa-donate text-white"></i>
                                        </div>
                                    </div>
                                    <div>
                                        <div class="small text-gray-500">December 7, 2019</div>
                                        $290.29 has been deposited into your account!
                                    </div>
                                </a>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="mr-3">
                                        <div class="icon-circle bg-warning">
                                            <i class="fas fa-exclamation-triangle text-white"></i>
                                        </div>
                                    </div>
                                    <div>
                                        <div class="small text-gray-500">December 2, 2019</div>
                                        Spending Alert: We've noticed unusually high spending for your account.
                                    </div>
                                </a>
                                <a class="dropdown-item text-center small text-gray-500" href="#">Show All Alerts</a>
                            </div>
                        </li>

                        <!-- Nav Item - Messages -->
                        <li class="nav-item dropdown no-arrow mx-1">
                            <a class="nav-link dropdown-toggle" href="#" id="messagesDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-envelope fa-fw"></i>
                                <!-- Counter - Messages -->
                                <span class="badge badge-danger badge-counter">7</span>
                            </a>
                            <!-- Dropdown - Messages -->
                            <div class="dropdown-list dropdown-menu dropdown-menu-right shadow animated--grow-in"
                                aria-labelledby="messagesDropdown">
                                <h6 class="dropdown-header">
                                    Message Center
                                </h6>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="dropdown-list-image mr-3">
                                        <img class="rounded-circle" src="img/undraw_profile_1.svg"
                                            alt="...">
                                        <div class="status-indicator bg-success"></div>
                                    </div>
                                    <div class="font-weight-bold">
                                        <div class="text-truncate">Hi there! I am wondering if you can help me with a
                                            problem I've been having.</div>
                                        <div class="small text-gray-500">Emily Fowler · 58m</div>
                                    </div>
                                </a>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="dropdown-list-image mr-3">
                                        <img class="rounded-circle" src="img/undraw_profile_2.svg"
                                            alt="...">
                                        <div class="status-indicator"></div>
                                    </div>
                                    <div>
                                        <div class="text-truncate">I have the photos that you ordered last month, how
                                            would you like them sent to you?</div>
                                        <div class="small text-gray-500">Jae Chun · 1d</div>
                                    </div>
                                </a>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="dropdown-list-image mr-3">
                                        <img class="rounded-circle" src="img/undraw_profile_3.svg"
                                            alt="...">
                                        <div class="status-indicator bg-warning"></div>
                                    </div>
                                    <div>
                                        <div class="text-truncate">Last month's report looks great, I am very happy with
                                            the progress so far, keep up the good work!</div>
                                        <div class="small text-gray-500">Morgan Alvarez · 2d</div>
                                    </div>
                                </a>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="dropdown-list-image mr-3">
                                        <img class="rounded-circle" src="https://source.unsplash.com/Mv9hjnEUHR4/60x60"
                                            alt="...">
                                        <div class="status-indicator bg-success"></div>
                                    </div>
                                    <div>
                                        <div class="text-truncate">Am I a good boy? The reason I ask is because someone
                                            told me that people say this to all dogs, even if they aren't good...</div>
                                        <div class="small text-gray-500">Chicken the Dog · 2w</div>
                                    </div>
                                </a>
                                <a class="dropdown-item text-center small text-gray-500" href="#">Read More Messages</a>
                            </div>
                        </li>

                        <div class="topbar-divider d-none d-sm-block"></div>

                        <!-- Nav Item - User Information -->
                        <#if loggedIn>
                        <script>
                            /** @type {WebSocket} */
                            const ws = new WebSocket("ws://localhost:8080");
                            window.ws = ws;
                            ws.onopen = () => {
                                ws.send(JSON.stringify({
                                    "iam": document.getElementById("userDropdown").querySelector("span").innerHTML.match(/<.*>(.*)/)[1]
                                }));
                            };
                            ws.onmessage = (mev) => {
                                const { sender, message } = JSON.parse(mev.data);
                                alert("NEUE MITTEILUNG:\n" + sender + "\n" + message);
                            }
                        </script>
                        <li class="nav-item dropdown no-arrow">
                            <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <span class="mr-2 d-none d-lg-inline text-gray-600 small"><i class="fas fa-user fa-sm fa-fw mr-2 text-gray-400"></i>${username}</span>
                            </a>
                            <!-- Dropdown - User Information -->
                            <div class="dropdown-menu dropdown-menu-right shadow animated--grow-in"
                                aria-labelledby="userDropdown">
                                <a class="dropdown-item" href="#" onclick="submitLogout()">
                                    <i class="fas fa-sign-out-alt fa-sm fa-fw mr-2 text-gray-400"></i>
                                    Logout
                                </a>
                            </div>
                        </li>
                        <#else>
                        <button onclick="window.location.pathname='/login'" type="button" class="btn btn-outline-primary" style="height: 40px; position:relative; bottom: -15px;">
                            <i class="fas fa-sign-in fa-sm fa-fw mr-2 text-gray-400"></i>
                            Login
                        </button>
                        </#if>
                    </ul>
                </nav>
                <!-- End of Topbar -->
                <div id="corpus-index" style="display: flex;">
                    <form class="d-inline-block form-inline ml-md-3 my-2 my-md-0 mw-100 navbar-search">
                        <div class="input-group" style="margin: 10px;">
                            <input name="searchQuery" id="corpus-query" type="text" class="form-control bg-light border-0 small" placeholder="Suche"
                                aria-label="Search" aria-describedby="basic-addon2">
                        </div>
                        <div class="input-group d-flex flex-column" style="margin: 10px;">
                            <label>Von</label>
                            <input name="from" type="date" id="corpus-from" class="form-control w-100">
                        </div>
                        <div class="input-group d-flex flex-column" style="margin: 10px;">
                            <label style="padding-right: auto;">Bis</label>
                            <input name="until" type="date" id="corpus-until" class="form-control w-100">
                        </div>
                        <div class="input-group d-flex flex-column" style="margin: 10px;">
                            <label style="padding-right: auto;">Gruppierung</label>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" id="radio-day" value="day" name="group-radio">
                                <label class="form-check-label">
                                    Tag
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" id="radio-week" value="week" name="group-radio">
                                <label class="form-check-label">
                                    Woche
                                </label>
                            </div>
                            <div class="form-check disabled">
                                <input class="form-check-input" type="radio" id="radio-month" value="month" name="group-radio">
                                <label class="form-check-label">
                                    Monat
                                </label>
                            </div>
                            <div class="form-check disabled">
                                <input class="form-check-input" type="radio" id="radio-year" value="year" name="group-radio">
                                <label class="form-check-label">
                                    Jahr
                                </label>
                            </div>
                            <div class="form-check disabled">
                                <input class="form-check-input" type="radio" id="radio-year" value="none" name="group-radio" checked>
                                <label class="form-check-label">
                                    Keine Gruppierung
                                </label>
                            </div>
                        </div>
                        <button class="btn btn-sm btn-primary w-100" type="button" onclick="spawnCorpusSearch(this.parentNode.searchQuery.value, new Date(this.parentNode.from.value).getTime(), new Date(this.parentNode.until.value).getTime(), this.parentNode['group-radio'].value)">
                            <i class="fas fa-search fa-sm"></i>
                        </button>
                    </form>
                    <div id="corpus-results" class="w-100"></div>
                </div>
                <div id="visualization" style="display: none;">
                    <form class="d-inline-block form-inline ml-md-3 my-2 my-md-0 mw-100 navbar-search">
                        <div class="input-group" style="margin: 10px;">
                            <input name="searchQuery" id="search-query" type="text" class="form-control bg-light border-0 small" placeholder="Suche"
                                aria-label="Search" aria-describedby="basic-addon2">
                        </div>
                        <div class="input-group d-flex flex-column" style="margin: 10px;">
                            <label>Von</label>
                            <input name="from" type="date" id="search-from" class="form-control w-100">
                        </div>
                        <div class="input-group d-flex flex-column" style="margin: 10px;">
                            <label style="padding-right: auto;">Bis</label>
                            <input name="until" type="date" id="search-until" class="form-control w-100">
                        </div>
                        <button class="btn btn-sm btn-primary w-100" type="button" onclick="spawnPanel(this.parentNode.searchQuery.value, new Date(this.parentNode.from.value).getTime(), new Date(this.parentNode.until.value).getTime())">
                            <i class="fas fa-search fa-sm"></i>
                        </button>
                    </form>
                    <div id="search-panels" class="w-100"></div>
                </div>
                <div id="full-text" style="display: none;"></div>
                <div id="messaging" style="display: none;">
                    <form>
                        <div class="input-group m-2">
                            <span class="input-group-text">Empfänger</span>
                            <input type="text" class="form-control" name="recipient">
                        </div>
                        <div class="input-group m-2">
                            <span class="input-group-text">Nachricht</span>
                            <input type="text" class="form-control" name="message">
                        </div>
                        <div class="input-group m-2">
                            <button type="button" class="btn btn-sm btn-outline-primary" onclick="sendMessage(this.parentNode.parentNode.message.value, this.parentNode.parentNode.recipient.value)">Abschicken</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</body>
</html>