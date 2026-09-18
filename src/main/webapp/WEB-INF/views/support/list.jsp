<%@ include file="header.jspf" %>
<div class="support-heading"><div><p class="eyebrow">${adminView ? 'CUSTOMER CARE' : 'MY SUPPORT'}</p><h1><c:out value="${pageTitle}"/></h1><p class="muted">${adminView ? '문의의 근거를 확인하고, 고객에게 보낼 답변을 완성하세요.' : '상품과 주문에 대해 궁금한 점을 남겨 주세요.'}</p></div>
<c:if test="${not adminView}"><a class="support-button" href="${pageContext.request.contextPath}/account/inquiry/new">새 문의 작성 ↗</a></c:if></div>
<c:if test="${adminView and not aiEnabled}"><div class="support-notice">AI가 연결되지 않았습니다. 문의 접수와 직접 답변은 정상적으로 사용할 수 있습니다.</div></c:if>
<form method="get" class="support-filters"><label for="status">처리 상태</label><select id="status" name="status"><option value="">전체</option><option value="OPEN" ${statusFilter == 'OPEN' ? 'selected' : ''}>답변 대기</option><option value="ANSWERED" ${statusFilter == 'ANSWERED' ? 'selected' : ''}>답변 완료</option></select><button class="support-button secondary">조회</button></form>
<div class="inquiry-list">
<c:forEach var="q" items="${inquiries}">
<c:url var="detailUrl" value="${adminView ? '/admin/inquiry' : '/account/inquiry'}"><c:param name="id" value="${q.id}"/></c:url>
<a class="inquiry-card" href="<c:out value='${detailUrl}'/>"><div><span class="status-pill ${q.status == 'ANSWERED' ? 'answered' : ''}">${q.status == 'ANSWERED' ? '답변 완료' : '답변 대기'}</span><h2><c:out value="${q.title}"/></h2><p class="muted"><c:out value="${q.productName}"/> <c:out value="${q.orderNumber}"/></p></div><div class="inquiry-date"><fmt:formatDate value="${q.createdAt}" pattern="yyyy.MM.dd HH:mm"/><span aria-hidden="true"> ↗</span></div></a>
</c:forEach>
<c:if test="${empty inquiries}"><div class="empty-state"><h2>표시할 문의가 없습니다.</h2><p>다른 처리 상태를 선택하거나 새로운 문의를 남겨 보세요.</p></div></c:if>
</div>
<nav class="support-pagination" aria-label="문의 페이지">
<c:if test="${currentPage > 1}"><c:url var="prevUrl" value=""><c:param name="page" value="${currentPage-1}"/><c:param name="status" value="${statusFilter}"/></c:url><a href="<c:out value='${prevUrl}'/>">← 이전</a></c:if>
<span><c:out value="${currentPage}"/> 페이지</span>
<c:if test="${inquiries.size() == 20 and currentPage < 10000}"><c:url var="nextUrl" value=""><c:param name="page" value="${currentPage+1}"/><c:param name="status" value="${statusFilter}"/></c:url><a href="<c:out value='${nextUrl}'/>">다음 →</a></c:if>
</nav></main></body></html>
