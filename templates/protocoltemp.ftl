\documentclass{article}
\title{\textbf{${title}}\\
${id}\\
Anfang: ${starttime}\\
Ende: ${endtime}\\
Standort: ${place}\\
Wahlperiode: ${period}}
\date{${date}}
\begin{document}
\maketitle
\newpage
<#list agendaItems as agendaItem>${agendaItem}${lb}</#list>
ENDE
\end{document}
